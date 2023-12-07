package net.peng.vulpes.parser.algebraic.meta;

import lombok.ToString;

/**
 * 表列表.
 */
@ToString
public class ShowTableMetaNode extends ShowMetaNode {
  private static final String TABLE = "TABLE";

  @Override
  public String showColumnName() {
    return TABLE;
  }
}
