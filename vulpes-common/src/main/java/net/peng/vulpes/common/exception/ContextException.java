package net.peng.vulpes.common.exception;

/**
 * Description of ContextException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/16
 */
public class ContextException extends RuntimeException {
  public ContextException() {
  }

  public ContextException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public ContextException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public ContextException(String message) {
    super(message);
  }

  public ContextException(String message, Throwable cause) {
    super(message, cause);
  }

  public ContextException(Throwable cause) {
    super(cause);
  }

  public ContextException(String message, Throwable cause, boolean enableSuppression,
                          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
