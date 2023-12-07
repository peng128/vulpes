package net.peng.vulpes.parser.algebraic.meta;

import lombok.ToString;

/**
 * 数据库列表.
 */
@ToString
public class ShowSchemaMetaNode extends ShowMetaNode {
  private static final String SCHEMA = "SCHEMA";

  @Override
  public String showColumnName() {
    return SCHEMA;
  }
}
