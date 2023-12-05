package net.peng.vulpes.parser.algebraic.expression;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 字段名称表达式.
 */
@Getter
@EqualsAndHashCode(callSuper = false, exclude = "index")
public class ColumnNameExpr extends RelalgExpr {

  /**
   * 表别名，可为空.
   */
  private final String qualifier;
  /**
   * 字段名称.
   */
  private final String name;

  /**
   * 相对于输入代数表达式节点的字段索引. -1 标识无对应字段.
   */
  private int index = -1;

  protected ColumnNameExpr(final String qualifier, final String name) {
    this.qualifier = qualifier;
    this.name = name;
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeader) {
    long count = inputHeader.getColumns().stream().filter(column -> column.getName().equals(name))
            .count();
    if (count == 0) {
      throw new AstConvertorException("%s 找不到这个字段", this.toString());
    } else if (count == 1) {
      index = inputHeader.indexOf(name);
    } else {
      if (ObjectUtils.isEmpty(qualifier)) {
        throw new AstConvertorException("%s 不能找到唯一的一个字段.", this.toString());
      }
      index = inputHeader.indexOf(qualifier, name);
      if (index == -1) {
        throw new AstConvertorException("%s 找不到这个全名称字段", this.toString());
      }
    }
    return ColumnInfo.builder()
            .name(this.toString())
            .dataType(inputHeader.getColumns().get(index).getDataType())
            .build();
  }

  @Override
  public String toString() {
    if (ObjectUtils.isEmpty(qualifier)) {
      return name;
    }
    return String.format("%s.%s", qualifier, name);
  }

  /**
   * create.
   *
   * @param expr 表达式
   * @return new object.
   */
  public static ColumnNameExpr create(final IdentifierExpr expr) {
    if (expr.getIdentifiers().size() == 1) {
      return new ColumnNameExpr(null, expr.getIdentifiers().get(0));
    }
    if (expr.getIdentifiers().size() == 2) {
      return new ColumnNameExpr(expr.getIdentifiers().get(0), expr.getIdentifiers().get(1));
    }
    throw new AstConvertorException("More than 2 identifier, can't used in column.[%s]", expr);
  }
}
