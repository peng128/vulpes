package net.peng.vulpes.common.exception;

/**
 * Description of ComputeException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
public class ComputeException extends RuntimeException {
  public ComputeException() {
  }

  public ComputeException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public ComputeException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public ComputeException(String message) {
    super(message);
  }

  public ComputeException(String message, Throwable cause) {
    super(message, cause);
  }

  public ComputeException(Throwable cause) {
    super(cause);
  }

  public ComputeException(String message, Throwable cause, boolean enableSuppression,
                          boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
