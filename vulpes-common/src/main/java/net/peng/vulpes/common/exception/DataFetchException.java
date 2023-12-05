package net.peng.vulpes.common.exception;

/**
 * Description of DataFetchException.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
public class DataFetchException extends RuntimeException {
  public DataFetchException() {
  }

  public DataFetchException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  public DataFetchException(final String message, Throwable cause, final Object... objects) {
    super(String.format(message, objects), cause);
  }

  public DataFetchException(String message) {
    super(message);
  }

  public DataFetchException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataFetchException(Throwable cause) {
    super(cause);
  }

  public DataFetchException(String message, Throwable cause, boolean enableSuppression,
                            boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
