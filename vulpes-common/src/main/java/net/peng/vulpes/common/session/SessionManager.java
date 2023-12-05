package net.peng.vulpes.common.session;

import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.peng.vulpes.common.configuration.Config;

/**
 * Description of SessionManager.
 * 用于存储这个会话的一些数据
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/8
 */
@Builder
@AllArgsConstructor
@Getter
public class SessionManager {

  @Builder.Default
  ClassLoader classLoader = SessionManager.class.getClassLoader();

  @Builder.Default
  private final String sessionId = "0000";

  /**
   * 配置项.
   */
  private final Config config;

  /**
   * 目前所在的目录.
   */
  @Builder.Default
  private final String currentCatalog = "default";

  /**
   * 目前所在的结构.
   */
  @Builder.Default
  private final String currentSchema = "default";

  public static SessionManager DEFAULT_SESSION_MANAGER =
          SessionManager.builder().config(new Config(new Properties())).build();
}
