package net.peng.vulpes.runtime.function.execute.arrow;

import java.util.List;
import lombok.ToString;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.runtime.function.execute.ExpressionExecutor;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.utils.VectorBuilderUtils;
import org.apache.arrow.vector.FieldVector;

/**
 * Description of ConstantProjection.
 * 语句中的常量.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/28
 */
@ToString
public class ArrowConstantExpression
        implements ExpressionExecutor<List<FieldVector>, FieldVector> {

  private static final int DEFAULT_ROW_COUNT = 1;
  private final Object value;
  private final ColumnInfo columnInfo;

  public ArrowConstantExpression(Object value, ColumnInfo columnInfo) {
    this.value = value;
    this.columnInfo = columnInfo;
  }

  @Override
  public FieldVector execute(List<FieldVector> valueVectorList, MemorySpace memorySpace) {
    FieldVector fieldVector = VectorBuilderUtils.buildFiledVector(columnInfo, memorySpace);
    int rowCount = DEFAULT_ROW_COUNT;
    if (!ObjectUtils.isEmpty(valueVectorList)) {
      rowCount = valueVectorList.get(0).getValueCount();
    }
    fieldVector.setInitialCapacity(rowCount);
    fieldVector.reAlloc();
    for (int i = 0; i < rowCount; i++) {
      VectorBuilderUtils.setFieldVector(columnInfo, fieldVector, i, value);
    }
    fieldVector.setValueCount(rowCount);
    return fieldVector;
  }
}
