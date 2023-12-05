package net.peng.vuples.parser.utils;

import java.util.List;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.BooleanType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FunctionRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.FunctionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of FunctionUtilsTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/29
 */
public class FunctionUtilsTests {

  @Test
  public void intSingleInputFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_single",
            List.of(ColumnNameExpr.create(IdentifierExpr.create("c"))),
            SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Integer.class, returnType);
  }

  @Test
  public void childClassInputFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_child",
            List.of(ColumnNameExpr.create(IdentifierExpr.create("c"))),
            SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Integer.class, returnType);
  }

  @Test
  public void multiInputFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_multi",
            List.of(ColumnNameExpr.create(IdentifierExpr.create("c")),
                    ColumnNameExpr.create(IdentifierExpr.create("b"))),
            SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Long.class, returnType);
  }

  @Test
  public void noInputFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_non",
            List.of(), SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Integer.class, returnType);
  }

  @Test
  public void multiEvalFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_multi_eval",
            List.of(ColumnNameExpr.create(IdentifierExpr.create("c"))),
            SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Integer.class, returnType);
  }

  @Test
  public void nestedFunctionTypeCheck() {
    FunctionRef functionRef = FunctionRef.create("test_function_multi_eval",
            List.of(FunctionRef.create("test_function_multi",
                    List.of(ColumnNameExpr.create(IdentifierExpr.create("c")),
                            ColumnNameExpr.create(IdentifierExpr.create("b"))),
                    SessionManager.DEFAULT_SESSION_MANAGER)),
            SessionManager.DEFAULT_SESSION_MANAGER);
    Class<?> returnType = FunctionUtils.checkFunctionType(functionRef, buildTestHeader());
    Assert.assertEquals(Integer.class, returnType);
  }

  private RowHeader buildTestHeader() {
    List<ColumnInfo> columnInfoList =
            List.of(ColumnInfo.builder().name("a").dataType(new VarcharType()).build(),
                    ColumnInfo.builder().name("b").dataType(new BigIntType()).build(),
                    ColumnInfo.builder().name("c").dataType(new IntType()).build(),
                    ColumnInfo.builder().name("d").dataType(new BooleanType()).build());
    return new RowHeader(columnInfoList);
  }
}
