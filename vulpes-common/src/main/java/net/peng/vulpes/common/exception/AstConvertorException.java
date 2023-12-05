package net.peng.vulpes.common.exception;

/**
 * AST语法异常.
 */
public class AstConvertorException extends RuntimeException {

  /**
   * ASTConvertorException.
   */
  public AstConvertorException() {
  }

  /**
   * ASTConvertorException.
   *
   * @param message 错误信息.
   * @param objects 错误信息填充对象.
   */
  public AstConvertorException(final String message, final Object... objects) {
    super(String.format(message, objects));
  }

  /**
   * ASTConvertorException.
   *
   * @param message 错误信息
   */
  public AstConvertorException(final String message) {
    super(message);
  }

  /**
   * ASTConvertorException.
   *
   * @param message 错误信息
   * @param cause   原因
   */
  public AstConvertorException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * ASTConvertorException.
   *
   * @param cause 原因
   */
  public AstConvertorException(final Throwable cause) {
    super(cause);
  }

  /**
   * ASTConvertorException.
   *
   * @param message            错误信息
   * @param cause              原因
   * @param enableSuppression  enableSuppression
   * @param writableStackTrace writableStackTrace
   */
  public AstConvertorException(final String message, final Throwable cause,
                               final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
