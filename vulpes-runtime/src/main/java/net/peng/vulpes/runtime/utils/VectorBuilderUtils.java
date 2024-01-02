package net.peng.vulpes.runtime.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.BooleanType;
import net.peng.vulpes.common.type.DateType;
import net.peng.vulpes.common.type.DoubleType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.IntervalType;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.common.type.time.IntervalValue;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.runtime.memory.MemorySpace;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.DateDayVector;
import org.apache.arrow.vector.DurationVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.types.TimeUnit;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.util.Text;

/**
 * Description of VectorBuilderUtils.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/20
 */
public class VectorBuilderUtils {

  /**
   * 通过输入构建一个{@link BigIntVector}.
   *
   * @param input       输入的数组.
   * @param memorySpace 内存空间
   * @param name        向量名称
   */
  public static BigIntVector buildBigIntVector(List<Long> input, MemorySpace memorySpace,
                                               String name) {
    BigIntVector bigIntVector = new BigIntVector(name, memorySpace.getAllocator());
    bigIntVector.setInitialCapacity(input.size());
    bigIntVector.reAlloc();
    for (int i = 0; i < input.size(); i++) {
      bigIntVector.set(i, input.get(i));
    }
    bigIntVector.setValueCount(input.size());
    return bigIntVector;
  }

  /**
   * 通过输入构建一个{@link VarCharVector}.
   *
   * @param input       输入的数组.
   * @param memorySpace 内存空间
   * @param name        向量名称
   */
  public static VarCharVector buildVarCharVector(List<String> input, MemorySpace memorySpace,
                                                 String name) {
    VarCharVector varCharVector = new VarCharVector(name, memorySpace.getAllocator());
    varCharVector.setInitialCapacity(input.size());
    varCharVector.reAlloc();
    for (int i = 0; i < input.size(); i++) {
      varCharVector.set(i, input.get(i).getBytes(StandardCharsets.UTF_8));
    }
    varCharVector.setValueCount(input.size());
    return varCharVector;
  }

  /**
   * 传入字段元信息构建内存向量.
   */
  public static FieldVector buildFiledVector(ColumnInfo columnInfo,
                                             MemorySpace memorySpace) {
    if (columnInfo.getDataType() instanceof IntType) {
      return new IntVector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof BigIntType) {
      return new BigIntVector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof VarcharType) {
      return new VarCharVector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof BooleanType) {
      return new BitVector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof DateType) {
      return new DateDayVector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof IntervalType) {
      return new DurationVector(columnInfo.getName(),
          FieldType.notNullable(new ArrowType.Duration(TimeUnit.MILLISECOND)),
          memorySpace.getAllocator());
    }
    if (columnInfo.getDataType() instanceof DoubleType) {
      return new Float8Vector(columnInfo.getName(),
          memorySpace.getAllocator());
    }
    throw new ComputeException("找不到[%s]对应物理类型.", columnInfo);
  }

  /**
   * 传入字段元信息构建内存向量.
   */
  public static void setFieldVector(ColumnInfo columnInfo, FieldVector fieldVector,
                                    int rowIndex, Object data) {
    if (columnInfo.getDataType() instanceof IntType) {
      ((IntVector) fieldVector).set(rowIndex, (Integer) data);
      return;
    }
    if (columnInfo.getDataType() instanceof BigIntType) {
      ((BigIntVector) fieldVector).set(rowIndex, (Long) data);
      return;
    }
    if (columnInfo.getDataType() instanceof VarcharType) {
      ((VarCharVector) fieldVector).setSafe(rowIndex, checkAndConvert(data));
      return;
    }
    if (columnInfo.getDataType() instanceof BooleanType) {
      ((BitVector) fieldVector).set(rowIndex, ((Boolean) data) ? 1 : 0);
      return;
    }
    if (columnInfo.getDataType() instanceof DateType) {
      ((DateDayVector) fieldVector).set(rowIndex, (int) ((LocalDate) data).toEpochDay());
      return;
    }
    if (columnInfo.getDataType() instanceof IntervalType) {
      ((DurationVector) fieldVector).set(rowIndex, ((IntervalValue) data).toEpochMillisecond());
      return;
    }
    if (columnInfo.getDataType() instanceof DoubleType) {
      ((Float8Vector) fieldVector).set(rowIndex, (Double) data);
      return;
    }
    throw new ComputeException("找不到[%s]对应物理类型.", columnInfo);
  }

  /**
   * 检查，如果不是{@link Text}格式就转换.
   */
  private static Text checkAndConvert(Object data) {
    if (data instanceof Text text) {
      return text;
    }
    return new Text(data.toString());
  }
}
