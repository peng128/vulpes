package net.peng.vulpes.common.model;

import java.util.List;
import lombok.Getter;
import net.peng.vulpes.common.exception.AstConvertorException;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * 表标识符号.
 * “目录.结构.表名称” 的格式.
 */
@Getter
public class TableIdentifier {

  private final String catalog;
  private final String schema;
  private final String table;

  private TableIdentifier(String catalog, String schema, String table) {
    this.catalog = catalog;
    this.schema = schema;
    this.table = table;
  }

  @Override
  public String toString() {
    if (ObjectUtils.isNotNull(catalog, schema)) {
      return String.format("%s.%s.%s", catalog, schema, table);
    }
    if (ObjectUtils.isNotNull(schema)) {
      return String.format("%s.%s", schema, table);
    }
    return table;
  }

  /**
   * 创建新的表标识符.
   * 可处理不输入catalog或catalog与schema都不传入的情况.
   *
   * @param input 输入表明列表.
   * @return 表标识符.
   */
  public static TableIdentifier create(List<String> input) {
    switch (input.size()) {
      case 1:
        return new TableIdentifier(null, null, input.get(0));
      case 2:
        return new TableIdentifier(null, input.get(0), input.get(1));
      case 3:
        return new TableIdentifier(input.get(0), input.get(1), input.get(2));
      default:
        throw new AstConvertorException("Table identifier must be [catalog].[schema].table, but "
                + "now is: [%s]", input);
    }
  }

  /**
   * 创建新的表标识符.
   * 可处理不输入catalog或catalog与schema都不传入的情况.
   *
   * @param input 输入表明列表.
   * @return 表标识符.
   */
  public static TableIdentifier create(List<String> input, String currentCatalog,
                                       String currentSchema) {
    switch (input.size()) {
      case 1:
        return new TableIdentifier(currentCatalog, currentSchema, input.get(0));
      case 2:
        return new TableIdentifier(currentCatalog, input.get(0), input.get(1));
      case 3:
        return new TableIdentifier(input.get(0), input.get(1), input.get(2));
      default:
        throw new AstConvertorException("Table identifier must be [catalog].[schema].table, but "
                + "now is: [%s]", input);
    }
  }

  /**
   * 返回这个表标识符是否是全路径的.
   */
  public boolean fullTableName() {
    return ObjectUtils.isNotNull(catalog, schema, table);
  }
}
