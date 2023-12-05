package net.peng.vulpes.parser.algebraic.expression;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * Description of ParameterExpr.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/27
 */
@ToString(callSuper = true)
@Getter
public class ParameterExpr extends ColumnNameExpr {

  private static final String NULL = "NULL";

  private static final String PARAMETER_PREFIX = "@@";

  private final LiteralExpr value;

  protected ParameterExpr(String qualifier, String name, LiteralExpr value) {
    super(qualifier, name);
    this.value = value;
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeaders) {
    ColumnInfo columnInfo = value.fillColumnInfo(null);
    return ColumnInfo.builder().name(PARAMETER_PREFIX + super.toString())
            .dataType(columnInfo.getDataType()).build();
  }

  public static ParameterExpr create(ColumnNameExpr columnNameExpr, SessionManager sessionManager) {
    return new ParameterExpr(columnNameExpr.getQualifier(), columnNameExpr.getName(),
            getParameterFromConfig(sessionManager, columnNameExpr.getName()));
  }

  private static LiteralExpr getParameterFromConfig(SessionManager sessionManager, String name) {
    Object parameter = sessionManager.getConfig().get(name);
    if (ObjectUtils.isNull(parameter)) {
      return LiteralExpr.create(NULL);
    }
    return LiteralExpr.create(parameter.toString());
  }
}
