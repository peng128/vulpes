package net.peng.vulpes.runtime.struct;

import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;

/**
 * Description of Row.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/9
 */
@Data
public class Row {
  private final List<Object> data;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Row row = (Row) o;
    return Objects.equals(data, row.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
