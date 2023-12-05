package net.peng.vuples.jdbc.mysql.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;
import net.peng.vuples.jdbc.mysql.socket.JdbcServer;

/**
 * Description of CommandHandler.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/30
 */
@Slf4j
public class CommandHandler implements ResponseHandler {

  public static final byte[] OK = new byte[] { 7, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0 };

  private final Map<ChannelId, SessionManager> sessionKeeper;
  private final StatementExecutor statementExecutor;

  public CommandHandler(Map<ChannelId, SessionManager> sessionKeeper,
                        StatementExecutor statementExecutor) {
    this.sessionKeeper = sessionKeeper;
    this.statementExecutor = statementExecutor;
  }

  @Override
  public ByteBuf handler(ChannelHandlerContext ctx, Object msg) throws IllegalAccessException {
    if (!(msg instanceof ByteBuf byteBuf)) {
      throw new IllegalAccessException("连接数据处理错误.");
    }
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.getBytes(0, bytes);
    log.info("处理数据，类型为[{}]", bytes[4]);
    if (bytes[4] == JdbcServer.REQUEST_TYPE_CMD) {
      String statement = new String(bytes, 5, bytes.length - 5, StandardCharsets.UTF_8);
      // 这里去掉注释.
      statement = statement.replaceAll("/\\*.*?\\*/", "");
      log.info("开始执行: {}", statement);
      byte[] resultByte = statementExecutor.execute(statement,
              sessionKeeper.get(ctx.channel().id()));
      ByteBuf resultBuf = ctx.alloc().buffer(resultByte.length);
      resultBuf.writeBytes(resultByte);
      return resultBuf;
    } else if (bytes[4] == JdbcServer.REQUEST_TYPE_QUIT) {
      log.info("客户端主动关闭连接[{}]", ctx.channel().id());
      ctx.channel().close();
    }
    ByteBuffer buffer = ByteBuffer.allocate(OK.length);
    buffer.put(OK);
    ByteBuf resultBuf = ctx.alloc().buffer(buffer.array().length);
    resultBuf.writeBytes(buffer.array());
    return resultBuf;
  }
}
