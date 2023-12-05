package net.peng.vulpes.parser.algebraic.logical;

import java.util.List;
import lombok.Getter;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * 带有子节点的代数表达式节点.
 */
@Getter
public abstract class InputRelalgNode extends RelalgNode {

  // 子节点
  protected List<RelalgNode> inputs;

  @Override
  public String explain() {
    StringBuilder plainTree = new StringBuilder();
    plainTree.append(super.explain()).append("\n");
    if (ObjectUtils.isEmpty(inputs)) {
      return super.explain();
    }
    for (RelalgNode input : inputs) {
      plainTree.append(appendSpaceToInput(input.explain()));
    }
    return plainTree.toString();
  }

  private String appendSpaceToInput(String input) {
    StringBuilder plainTree = new StringBuilder();
    final String[] lines = input.split("\n");
    for (String line : lines) {
      plainTree.append("  ").append(line).append("\n");
    }
    return plainTree.toString();
  }
}
