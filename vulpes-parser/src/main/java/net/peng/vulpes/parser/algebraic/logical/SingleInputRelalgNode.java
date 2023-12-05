package net.peng.vulpes.parser.algebraic.logical;

import java.util.Collections;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;

/**
 * 单输入的代数表达式节点.
 * 子节点只有一个
 */
public abstract class SingleInputRelalgNode extends InputRelalgNode {

  public RelalgNode getInput() {
    return inputs.get(0);
  }

  protected RelalgNode setInput(RelalgNode relalgNode) {
    if (ObjectUtils.isNotNull(relalgNode.rowHeader)) {
      this.rowHeader = computeOutputHeader(relalgNode.rowHeader);
    }
    if (ObjectUtils.isNotNull(this.inputs)) {
      throw new AstConvertorException("%s only support one input[%s]. But adding [%s].", this,
              this.getInput(),
              relalgNode);
    }
    this.inputs = Collections.singletonList(relalgNode);
    return this;
  }

  /**
   * 通过输入数据计算输出数据的结构.
   */
  public abstract RowHeader computeOutputHeader(RowHeader inputHeader);
}
