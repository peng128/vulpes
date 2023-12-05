package net.peng.vulpes.parser.algebraic;

import java.io.Serializable;

/**
 * 关系表达式基类.
 */
public interface RelationAlgebraic extends Serializable {
  RelationAlgebraic merge(RelationAlgebraic relationAlgebraic);
}
