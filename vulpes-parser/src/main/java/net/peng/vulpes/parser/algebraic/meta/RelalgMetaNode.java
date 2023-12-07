package net.peng.vulpes.parser.algebraic.meta;

import net.peng.vulpes.parser.algebraic.RelationAlgebraic;

/**
 * 元数据表达式节点.
 */
public abstract class RelalgMetaNode implements RelationAlgebraic {

  @Override
  public RelationAlgebraic merge(RelationAlgebraic relationAlgebraic) {
    return this;
  }
}
