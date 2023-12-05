package net.peng.vulpes.parser.algebraic.logical;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.expression.IdentifierExpr;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * 扫描关系表达式节点.
 */
@ToString(exclude = "tableMeta")
@Getter
public class RelalgScan extends RelalgNode {

  private final TableIdentifier tableIdentifier;
  private final TableMeta tableMeta;

  private RelalgScan(TableIdentifier tableIdentifier, TableMeta tableMeta) {
    this.tableIdentifier = tableIdentifier;
    this.tableMeta = tableMeta;
    if (ObjectUtils.isNotNull(tableMeta)) {
      this.rowHeader = new RowHeader(tableMeta);
    }
  }

  public static RelalgScan create(TableIdentifier tableIdentifier) {
    return new RelalgScan(tableIdentifier, null);
  }

  public static RelalgScan create(TableIdentifier tableIdentifier, TableMeta tableMeta) {
    return new RelalgScan(tableIdentifier, tableMeta);
  }

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    if (relationAlgebraic instanceof SingleInputRelalgNode) {
      return ((SingleInputRelalgNode) relationAlgebraic).setInput(this);
    }
    return super.merge(relationAlgebraic);
  }

  @Override
  public RelalgNode accept(RelalgNodeVisitor relalgNodeVisitor) {
    return relalgNodeVisitor.visit(this);
  }
}
