package net.peng.vulpes.parser.algebraic.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.function.OperatorSymbol;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.utils.DataTypeEstimateUtils;
import net.peng.vulpes.parser.utils.FunctionUtils;

/**
 * 函数引用表达式.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class FunctionRef extends RelalgExpr {
  private String operator;
  private List<RelalgExpr> items;
  /**
   * 运算的优先级，用于调整运算顺序.
   * 如四则运算中，先计算括弧中的值.
   * 如遇到括弧，则将优先级加一.
   */
  private int priority = 0;
  private final SessionManager sessionManager;
  private Function function;

  /**
   * 函数表达式.
   */
  public FunctionRef(String operator, List<RelalgExpr> items, int priority,
                     SessionManager sessionManager) {
    this.operator = operator;
    this.items = items;
    this.priority = priority;
    this.sessionManager = sessionManager;
  }

  public void addPriority() {
    priority++;
  }

  public void addPriority(int priority) {
    this.priority += priority;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(operator).append("(");
    for (RelalgExpr item : items) {
      sb.append(item.toString()).append(",");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append(")");
    return sb.toString();
  }

  public static FunctionRef create(String operator, List<RelalgExpr> items,
                                   SessionManager sessionManager) {
    return new FunctionRef(operator, items, 0, sessionManager);
  }

  public static FunctionRef create(String operator, SessionManager sessionManager,
                                   RelalgExpr... items) {
    return new FunctionRef(operator, Arrays.asList(items), 0, sessionManager);
  }

  @Override
  public ColumnInfo fillColumnInfo(RowHeader inputHeader) {
    DataType dataType;
    for (RelalgExpr item : items) {
      item.fillColumnInfo(inputHeader);
    }
    //TODO: 这里考虑一下case如何实现.
    if (operator.equalsIgnoreCase(OperatorSymbol.CASE.value)) {
      List<ColumnInfo> inputColumns = items.stream().map(item -> item.fillColumnInfo(inputHeader))
              .toList();
      dataType = DataTypeEstimateUtils.functionResultSpeculate(operator,
              inputColumns.stream().map(ColumnInfo::getDataType).toList());
    } else if (operator.equalsIgnoreCase(OperatorSymbol.CAST.name())) {
      dataType = DataTypeEstimateUtils.convertFromPlain(items.get(1).toString());
      if (ObjectUtils.isNull(function)) {
        function = FunctionUtils.getFunction(operator, sessionManager.getClassLoader());
      }
    } else {
      if (ObjectUtils.isNull(function)) {
        function = FunctionUtils.getFunction(operator, sessionManager.getClassLoader());
      }
      dataType = DataTypeEstimateUtils.functionResultSpeculate(this, inputHeader);
    }
    return ColumnInfo.builder().name(this.toString()).dataType(dataType).build();
  }

  /**
   * 构造器.
   */
  public static class Builder {
    private String operator;
    private List<RelalgExpr> items = new ArrayList<>();
    private int priority = 0;
    private SessionManager sessionManager;

    public Builder operator(String operator) {
      this.operator = operator;
      return this;
    }

    public Builder addItem(RelalgExpr relalgExpr) {
      items.add(relalgExpr);
      return this;
    }

    public Builder sessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
      return this;
    }

    public Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    public FunctionRef build() {
      return new FunctionRef(operator, items, priority, sessionManager);
    }
  }
}
