package net.peng.vulpes.common.session;

/**
 * Description of SessionManagerFactory.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
public class SessionManagerFactory {
  public SessionManager create() {
    return SessionManager.DEFAULT_SESSION_MANAGER;
  }
}
