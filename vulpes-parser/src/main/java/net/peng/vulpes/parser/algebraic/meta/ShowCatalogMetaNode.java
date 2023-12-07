package net.peng.vulpes.parser.algebraic.meta;

import lombok.ToString;

/**
 * 目录列表.
 */
@ToString
public class ShowCatalogMetaNode extends ShowMetaNode {
  private static final String CATALOG = "CATALOG";

  @Override
  public String showColumnName() {
    return CATALOG;
  }
}
