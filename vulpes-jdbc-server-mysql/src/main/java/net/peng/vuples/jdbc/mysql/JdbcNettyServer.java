package net.peng.vuples.jdbc.mysql;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.session.SessionManagerFactory;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;

/**
 * Description of NettyServer.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/19
 */
@Slf4j
public class JdbcNettyServer {

  private Channel channel;

  private final StatementExecutor statementExecutor;
  private final SessionManagerFactory sessionManagerFactory;

  public JdbcNettyServer(StatementExecutor statementExecutor,
                         SessionManagerFactory sessionManagerFactory) {
    this.statementExecutor = statementExecutor;
    this.sessionManagerFactory = sessionManagerFactory;
  }

  /**
   * 启动服务.
   */
  public void start() throws InterruptedException {
    // 主线程使用的对话，主要用于配置读取
    Config config =  sessionManagerFactory.create().getConfig();
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup =
            new NioEventLoopGroup(config.get(ConfigItems.JDBC_WORKER_THREAD_NUM));
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
              .channel(NioServerSocketChannel.class)
              .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                  ChannelPipeline pipeline = ch.pipeline();
                  // 为每个Channel创建一个新的处理器实例
                  pipeline.addLast(new JdbcServerHandler(statementExecutor,
                          sessionManagerFactory));
                }
              });
      ChannelFuture f = b.bind(config.get(ConfigItems.JDBC_TPC_PORT)).sync();
      log.info("Netty jdbc 服务端启动");
      channel = f.channel();
      f.channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      log.info("关闭netty server并回收资源");
    }
  }

  /**
   * 判断服务是否正在运行.
   */
  public boolean isRunning() {
    if (ObjectUtils.isNull(channel)) {
      return false;
    }
    return channel.isOpen();
  }

  /**
   * 关闭服务.
   */
  public void close() {
    if (ObjectUtils.isNotNull(channel)) {
      channel.close();
    }
  }
}


