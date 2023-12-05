package net.peng.vuples.jdbc.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelPromise;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.session.SessionManagerFactory;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;
import net.peng.vuples.jdbc.mysql.handler.AuthHandler;
import net.peng.vuples.jdbc.mysql.handler.CommandHandler;
import net.peng.vuples.jdbc.mysql.handler.ResponseHandler;
import net.peng.vuples.jdbc.mysql.mycat.BufferUtil;
import net.peng.vuples.jdbc.mysql.socket.JdbcServer;

/**
 * mysql协议命令类型资料：https://zhuanlan.zhihu.com/p/620225986
 * 官方文档: https://dev.mysql.com/doc/dev/mysql-server/latest/PAGE_PROTOCOL.html
 */
@Slf4j
public class JdbcServerHandler extends ChannelDuplexHandler {

  Map<ChannelId, SessionManager> sessionKeeper = new ConcurrentHashMap<>();

  private final StatementExecutor statementExecutor;
  private final SessionManagerFactory sessionManagerFactory;

  ResponseHandler handler = new AuthHandler(this);

  public void switchToCommandHandler() {
    log.debug("----->switch");
    handler = new CommandHandler(sessionKeeper, statementExecutor);
  }

  public JdbcServerHandler(StatementExecutor statementExecutor,
                           SessionManagerFactory sessionManagerFactory) {
    this.statementExecutor = statementExecutor;
    this.sessionManagerFactory = sessionManagerFactory;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("新链接[{}]握手, 当前连接数量： {}", ctx.channel().id(), sessionKeeper.size());
    sessionKeeper.put(ctx.channel().id(), sessionManagerFactory.create());
    ctx.writeAndFlush(genHeader(ctx));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.info("关闭连接[{}]", ctx.channel().id());
    sessionKeeper.remove(ctx.channel().id());
    super.channelInactive(ctx);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    log.debug("channel Read");
    if (!(msg instanceof ByteBuf byteBuf)) {
      throw new IllegalAccessException("连接数据处理错误.");
    }
    final ByteBuf result = handler.handler(ctx, msg);
    if (ObjectUtils.isNotNull(result)) {
      ctx.writeAndFlush(result);
    } else {
      super.channelRead(ctx, msg);
    }
  }

  private ByteBuf genHeader(ChannelHandlerContext ctx) throws IOException {
    JdbcServer.HeaderInfo headerInfo = JdbcServer.write();
    ByteBuffer buffer = headerInfo.getHeader();
    ByteBuf byteBuf = ctx.alloc().buffer(buffer.array().length);
    byteBuf.writeBytes(buffer.array());
    return byteBuf;
  }
}
