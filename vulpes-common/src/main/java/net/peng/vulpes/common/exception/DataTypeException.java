package net.peng.vulpes.common.exception;

/**
 * Description of DataTypeException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class DataTypeException extends RuntimeException {
  public DataTypeException() {
  }

  public DataTypeException(String message) {
    super(message);
  }

  public DataTypeException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public DataTypeException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public DataTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataTypeException(Throwable cause) {
    super(cause);
  }

  public DataTypeException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
