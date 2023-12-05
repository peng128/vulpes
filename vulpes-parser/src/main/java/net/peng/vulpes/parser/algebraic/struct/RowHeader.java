package net.peng.vulpes.parser.algebraic.struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.exception.TableException;
import net.peng.vulpes.common.type.DataType;

/**
 * Description of RowHeader.
 * 用于描述这个关系节点输出的行结构.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/7
 */
@Getter
@EqualsAndHashCode
public class RowHeader {
  private final List<ColumnInfo> columns;

  /**
   * 只有在多表输入节点才会存在，用来标识字段来源于哪个表或子逻辑.
   */
  private final List<ColumnFullName> fullOutputNames;

  /**
   * 初始化.
   */
  public RowHeader(List<ColumnInfo> columns) {
    this.columns = columns;
    this.fullOutputNames = columns.stream()
            .map(name -> ColumnFullName.builder().column(name).alias("").build())
            .collect(Collectors.toList());
    ;
  }

  /**
   * 初始化.
   */
  public RowHeader(TableMeta tableMeta) {
    List<String> names = tableMeta.getFieldNames();
    List<DataType> dataTypes = tableMeta.getFieldTypes();
    if (names.size() != dataTypes.size()) {
      throw new TableException("字段类型和字段类型个数不匹配: %s", tableMeta);
    }
    List<ColumnInfo> columns = new ArrayList<>(names.size());
    for (int i = 0; i < names.size(); i++) {
      columns.add(ColumnInfo.builder().name(names.get(i)).dataType(dataTypes.get(i)).build());
    }
    this.columns = columns;
    this.fullOutputNames = columns.stream()
            .map(name -> ColumnFullName.builder().column(name).alias("").build())
            .collect(Collectors.toList());
  }

  /**
   * 初始化. 带有别名.
   */
  public RowHeader(List<ColumnInfo> columns, String alias) {
    this.columns = columns;
    this.fullOutputNames = columns.stream()
            .map(column -> ColumnFullName.builder().column(column).alias(alias).build())
            .collect(Collectors.toList());
    ;
  }

  /**
   * 初始化一个带有别名的表头.
   */
  public RowHeader(RowHeader rowHeader, Optional<String> alias) {
    this.columns = rowHeader.getColumns();
    if (alias.isPresent()) {
      this.fullOutputNames =
              columns.stream()
                      .map(name -> ColumnFullName.builder().column(name).alias(alias.get()).build())
                      .collect(Collectors.toList());
    } else {
      this.fullOutputNames = rowHeader.getFullOutputNames();
    }
  }

  /**
   * 在这个表头的基础上，添加一个带有别名的表头.
   */
  public RowHeader addRowHeader(RowHeader rowHeader, String alias) {
    this.columns.addAll(rowHeader.getColumns());
    this.fullOutputNames.addAll(rowHeader.getColumns().stream()
            .map(column -> ColumnFullName.builder().column(column).alias(alias).build())
            .toList());
    return this;
  }

  /**
   * 通过字段名称找到对应字段的索引.
   * 返回-1标识没有找到.
   */
  public Integer indexOf(String columnName) {
    for (int i = 0; i < columns.size(); i++) {
      if (columns.get(i).getName().equals(columnName)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 通过字段名称和字段别名找到对应字段的索引.
   * 返回-1标识没有找到.
   */
  public Integer indexOf(String alias, String columnName) {
    for (int i = 0; i < fullOutputNames.size(); i++) {
      if (fullOutputNames.get(i).getAlias().equals(alias)
              && fullOutputNames.get(i).getColumn().getName().equals(columnName)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 通过字段名称和字段别名找到对应字段的索引, 返回对应别名表内部的字段顺序.
   * 返回-1标识没有找到.
   */
  public Integer aliasInternalIndexOf(String alias, String columnName) {
    String previousAlias = "";
    int aliasIndex = 0;
    for (ColumnFullName fullOutputName : fullOutputNames) {
      if (!fullOutputName.getAlias().equals(previousAlias)) {
        aliasIndex = 0;
        previousAlias = fullOutputName.getAlias();
      }
      if (fullOutputName.getAlias().equals(alias)
              && fullOutputName.getColumn().getName().equals(columnName)) {
        return aliasIndex;
      }
      aliasIndex++;
    }
    return -1;
  }

  /**
   * 通过字段名称和字段别名找到对应字段所在第几个别名表中.
   * 返回-1标识没有找到.
   */
  public Integer aliasIndexOf(String alias, String columnName) {
    String previousAlias = "";
    int aliasIndex = -1;
    for (ColumnFullName fullOutputName : fullOutputNames) {
      if (!fullOutputName.getAlias().equals(previousAlias)) {
        aliasIndex++;
        previousAlias = fullOutputName.getAlias();
      }
      if (fullOutputName.getAlias().equals(alias)
              && fullOutputName.getColumn().getName().equals(columnName)) {
        return aliasIndex;
      }
    }
    return -1;
  }

  public static final RowHeader EMPTY_ROW_HEADER = new RowHeader(new ArrayList<>());

  /**
   * 带有别名的字段.
   */
  @Getter
  @Builder
  @AllArgsConstructor
  public static class ColumnFullName {
    private final String alias;
    private final ColumnInfo column;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ColumnFullName that = (ColumnFullName) o;
      return Objects.equals(alias, that.alias) && Objects.equals(column, that.column);
    }

    @Override
    public int hashCode() {
      return Objects.hash(alias, column);
    }
  }
}
