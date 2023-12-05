package net.peng.vulpes.parser.antlr4;

import net.peng.vulpes.common.exception.AstConvertorException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Description of ParseErrorListener.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class ParseErrorListener extends BaseErrorListener {
  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                          int charPositionInLine, String msg, RecognitionException e) {
    super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    String errorMsg = String.format("在[%s]行[%s]列，有语法错误: %s", line, charPositionInLine, msg);
    throw new AstConvertorException(errorMsg, e);
  }
}
