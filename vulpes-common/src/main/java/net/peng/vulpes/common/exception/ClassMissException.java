package net.peng.vulpes.common.exception;

/**
 * Description of ClassNotFoundException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class ClassMissException extends RuntimeException {
  public ClassMissException() {
  }

  public ClassMissException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public ClassMissException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public ClassMissException(String message) {
    super(message);
  }

  public ClassMissException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClassMissException(Throwable cause) {
    super(cause);
  }

  public ClassMissException(String message, Throwable cause, boolean enableSuppression,
                            boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
