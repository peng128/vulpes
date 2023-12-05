package net.peng.vuples.jdbc.mysql.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Description of ResponseHandler.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/30
 */
public interface ResponseHandler {

  /**
   * 处理消息.
   */
  ByteBuf handler(ChannelHandlerContext ctx, Object msg) throws IllegalAccessException;
}
