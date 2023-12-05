package net.peng.vulpes.common.exception;

/**
 * Description of ConfigMissingException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class ConfigMissingException extends RuntimeException {
  public ConfigMissingException() {
  }

  public ConfigMissingException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public ConfigMissingException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public ConfigMissingException(String message) {
    super(message);
  }

  public ConfigMissingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigMissingException(Throwable cause) {
    super(cause);
  }

  public ConfigMissingException(String message, Throwable cause, boolean enableSuppression,
                                boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
