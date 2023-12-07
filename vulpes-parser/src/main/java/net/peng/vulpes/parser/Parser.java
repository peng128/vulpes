package net.peng.vulpes.parser;

import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.antlr4.AlgebraicConvertVisitor;
import net.peng.vulpes.parser.antlr4.ParseErrorListener;
import net.peng.vulpes.parser.antlr4.SQL92Lexer;
import net.peng.vulpes.parser.antlr4.SQL92Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

/**
 * SQL语句的解析.
 */
@Slf4j
public class Parser {

  /**
   * 解析文本，并返回代数表达式.
   */
  public static RelationAlgebraic parse(String statement, CatalogManager catalogManager,
                                        SessionManager sessionManager) {
    long start = System.currentTimeMillis();
    SQL92Lexer exprLexer = new SQL92Lexer(CharStreams.fromString(statement));
    TokenStream tokenStream = new CommonTokenStream(exprLexer);
    SQL92Parser sql92Parser = new SQL92Parser(tokenStream);
    sql92Parser.addErrorListener(new ParseErrorListener());

    AlgebraicConvertVisitor algebraicConvertVisitor = new AlgebraicConvertVisitor(catalogManager,
            sessionManager);
    final RelationAlgebraic visit = algebraicConvertVisitor.visit(sql92Parser.query());
    log.debug("[{}] internal parser: {} ms", sessionManager.getSessionId(),
            System.currentTimeMillis() - start);
    return visit;
  }
}
