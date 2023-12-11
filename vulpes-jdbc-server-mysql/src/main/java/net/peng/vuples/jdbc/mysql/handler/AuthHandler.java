package net.peng.vuples.jdbc.mysql.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import net.peng.vuples.jdbc.mysql.JdbcServerHandler;
import net.peng.vuples.jdbc.mysql.mycat.AuthPacket;
import net.peng.vuples.jdbc.mysql.mycat.BufferUtil;
import net.peng.vuples.jdbc.mysql.socket.JdbcServer;

/**
 * Description of AuthHandler.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/30
 */
@Slf4j
public class AuthHandler implements ResponseHandler {

  private final JdbcServerHandler jdbcServerHandler;

  AuthPacket authPacket = null;

  public AuthHandler(JdbcServerHandler jdbcServerHandler) {
    this.jdbcServerHandler = jdbcServerHandler;
  }

  @Override
  public ByteBuf handler(ChannelHandlerContext ctx, Object msg) throws IllegalAccessException {
    // 这里先跳过了验证，直接返回验证成功.
    if (!(msg instanceof ByteBuf byteBuf)) {
      throw new IllegalAccessException("连接数据处理错误.");
    }
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.getBytes(0, bytes);
    if (authPacket == null) {
      authPacket = new AuthPacket();
      authPacket.read(bytes);
    }
    log.debug("Auth: {}", authPacket);
    if (authPacket.clientAuthPlugin.equalsIgnoreCase(JdbcServer.DEFAULT_AUTH_PLUGIN_NAME_STRING)) {
      final byte packetId = 3;
      int size = 3;
      size += JdbcServer.DEFAULT_AUTH_PLUGIN_NAME.length;
      //size += jdbcServerHandler.getSeed().length;
      ByteBuffer buffer = ByteBuffer.allocate(size);
      BufferUtil.writeUb3(buffer, size);
      buffer.put(packetId);
      buffer.put(JdbcServer.STATUS);
      BufferUtil.writeWithNull(buffer, JdbcServer.DEFAULT_AUTH_PLUGIN_NAME);
      //BufferUtil.writeWithNull(buffer, jdbcServerHandler.getSeed());
      ByteBuf resultBuf = ctx.alloc().buffer(buffer.array().length);
      resultBuf.writeBytes(buffer.array());
      return resultBuf;
    } else {
      ByteBuffer buffer = ByteBuffer.allocate(JdbcServer.AUTH_OK.length);
      buffer.put(JdbcServer.AUTH_OK);
      ByteBuf resultBuf = ctx.alloc().buffer(buffer.array().length);
      resultBuf.writeBytes(buffer.array());
      jdbcServerHandler.switchToCommandHandler();
      return resultBuf;
    }
  }
}
