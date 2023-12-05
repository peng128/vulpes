package net.peng.vulpes.runtime.struct.data;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.runtime.struct.Row;

/**
 * Description of RowSegment.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
@ToString
@EqualsAndHashCode
@Getter
public class OutputSegment implements Segment<List<Row>> {

  private List<Row> rows;
  private final List<ColumnInfo> columns;

  public OutputSegment(List<ColumnInfo> columns) {
    this.columns = columns;
  }

  public OutputSegment(List<Row> rows, List<ColumnInfo> columns) {
    this.rows = rows;
    this.columns = columns;
  }

  @Override
  public List<Row> get() {
    return rows;
  }

  @Override
  public void put(List<Row> rows) {
    this.rows = rows;
  }
}
