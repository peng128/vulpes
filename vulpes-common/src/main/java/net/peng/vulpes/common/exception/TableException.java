package net.peng.vulpes.common.exception;

/**
 * Description of TableException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class TableException extends RuntimeException {
  public TableException() {
  }

  public TableException(String message) {
    super(message);
  }

  public TableException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public TableException(String message, Throwable cause) {
    super(message, cause);
  }

  public TableException(Throwable cause) {
    super(cause);
  }

  public TableException(String message, Throwable cause, boolean enableSuppression,
                        boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
