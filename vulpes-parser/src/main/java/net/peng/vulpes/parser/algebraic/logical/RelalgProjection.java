package net.peng.vulpes.parser.algebraic.logical;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.ColumnNameExpr;
import net.peng.vulpes.parser.algebraic.expression.FullColumnRef;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.expression.RelalgExpr;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * 投影节点.
 */
@Getter
@ToString
public class RelalgProjection extends SingleInputRelalgNode {
  private final List<RelalgExpr> projects;

  private RelalgProjection(List<RelalgExpr> projects) {
    this.projects = projects;
  }

  public static RelalgProjection create(List<RelalgExpr> items) {
    return new RelalgProjection(items);
  }

  @Override
  public RowHeader computeOutputHeader(RowHeader inputHeader) {
    final List<ColumnInfo> columnInfoList = projects.stream().map(expr ->
            expr.fillColumnInfo(inputHeader)).collect(Collectors.toList());
    return new RowHeader(columnInfoList);
  }

  /**
   * 构建方法类.
   */
  public static class Builder {
    private final List<RelalgExpr> projects = new ArrayList<>();

    public Builder addItem(RelalgExpr relalgExpr) {
      projects.add(relalgExpr);
      return this;
    }

    public RelalgProjection build() {
      return new RelalgProjection(projects);
    }
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    //这里会默认从投影节点开始遍历进行merge，单投影节点是一个单输入节点，如果已经有了一个输入，尝试调用对应合并节点的merge方法.
    if (relationAlgebraic instanceof RelalgProjection) {
      throw new AstConvertorException("Projection[%s] can't merge Projection node.", this);
    }
    RelalgNode inputNode = ObjectUtils.checkClass(relationAlgebraic, RelalgNode.class,
            AstConvertorException.class);
    if (ObjectUtils.isNotNull(this.getInputs())) {
      return relationAlgebraic.merge(this);
    }
    if (relationAlgebraic instanceof RelalgNode) {
      RowHeader inputRowHeader = ((RelalgNode) relationAlgebraic).rowHeader;
      if (ObjectUtils.isNotNull(inputRowHeader)) {
        return expandFullColumnRef(inputRowHeader).setInput(inputNode);
      }
    }
    return this.setInput(inputNode);
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }

  /**
   * 将语句中select * 的投影展开.
   */
  private RelalgProjection expandFullColumnRef(RowHeader inputHeader) {
    final List<RelalgExpr> newProjects = new ArrayList<>();
    for (RelalgExpr project : projects) {
      if (project instanceof FullColumnRef) {
        newProjects.addAll(inputHeader.getColumns().stream().map(column -> ColumnNameExpr
                .create(IdentifierExpr.create(column.getName()))).toList());
      } else {
        newProjects.add(project);
      }
    }
    return RelalgProjection.create(newProjects);
  }
}
