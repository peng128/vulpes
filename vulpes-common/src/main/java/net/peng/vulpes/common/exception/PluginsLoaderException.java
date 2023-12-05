package net.peng.vulpes.common.exception;

/**
 * Description of PluginsLoaderException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class PluginsLoaderException extends RuntimeException {
  public PluginsLoaderException() {
    super();
  }

  public PluginsLoaderException(String message) {
    super(message);
  }

  public PluginsLoaderException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public PluginsLoaderException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public PluginsLoaderException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginsLoaderException(Throwable cause) {
    super(cause);
  }

  protected PluginsLoaderException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
