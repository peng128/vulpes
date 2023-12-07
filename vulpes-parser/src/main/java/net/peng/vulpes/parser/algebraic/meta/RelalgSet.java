package net.peng.vulpes.parser.algebraic.meta;

import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;

/**
 * Description of RelalgSet.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
@Getter
@ToString
public class RelalgSet extends RelalgMetaNode {

  private final String parameter;

  private final String value;

  public RelalgSet(String parameter, String value) {
    this.parameter = parameter;
    this.value = value;
  }
}
