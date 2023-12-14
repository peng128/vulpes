package net.peng.vulpes.benchmark;

import io.trino.tpch.TpchColumn;
import io.trino.tpch.TpchEntity;
import io.trino.tpch.TpchTable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.DateDayVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.types.Types;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

/**
 * 生成TPCH的数据.
 * GPT生成.
 */
public class TpchDataSetGenerator {

  /**
   * TpchDataSetGenerator.
   */
  public static void main(String[] args) throws IOException {
    // Set the scale factor and output directory
    double scaleFactor = 1;
    String outputDir = "./tpch/";
    genTpchArrowData(scaleFactor, outputDir);
  }

  /**
   * 生成arrow测试数据数据.
   */
  public static void genTpchArrowData(double scaleFactor, String outputDir) throws IOException {
    // Generate TPC-H data and save it as Arrow
    for (TpchTable<?> table : TpchTable.getTables()) {
      generateAndSaveAsArrow(table, scaleFactor, outputDir);
    }
  }

  /**
   * TpchDataSetGenerator.
   */
  public static <T extends TpchEntity> void generateAndSaveAsArrow(
      TpchTable<T> table, double scaleFactor, String outputDir) throws IOException {
    // Generate TPC-H data
    Iterable<T> rows = table.createGenerator(scaleFactor, 1, 1);

    // Create Arrow schema
    List<Field> fields = new ArrayList<>();
    for (TpchColumn<T> column : table.getColumns()) {
      FieldType fieldType;
      switch (column.getType().getBase()) {
        case INTEGER:
          fieldType = new FieldType(true, Types.MinorType.INT.getType(), null);
          break;
        case DOUBLE:
          fieldType = new FieldType(true, Types.MinorType.FLOAT8.getType(), null);
          break;
        case DATE:
          fieldType = new FieldType(true, Types.MinorType.DATEDAY.getType(), null);
          break;
        case IDENTIFIER:
        case VARCHAR:
          fieldType = new FieldType(true, Types.MinorType.VARCHAR.getType(), null);
          break;
        default:
          throw new IllegalArgumentException("Unsupported data type: " + column.getType());
      }
      fields.add(new Field(column.getSimplifiedColumnName(), fieldType, null));
    }
    Schema schema = new Schema(fields);

    // Create Arrow vectors
    try (RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);
         VectorSchemaRoot root = VectorSchemaRoot.create(schema, allocator)) {
      for (FieldVector vector : root.getFieldVectors()) {
        vector.allocateNew();
      }

      // Fill Arrow vectors with TPC-H data
      int rowCount = 0;
      for (T row : rows) {
        String[] values = row.toLine().split("\\|");
        for (int i = 0; i < values.length; i++) {
          FieldVector vector = root.getVector(i);
          switch (vector.getMinorType()) {
            case INT:
              ((IntVector) vector).setSafe(rowCount, Integer.parseInt(values[i]));
              break;
            case FLOAT8:
              ((Float8Vector) vector).setSafe(rowCount, Double.parseDouble(values[i]));
              break;
            case DATEDAY:
              ((DateDayVector) vector).setSafe(rowCount,
                  LocalDate.parse(values[i]).getDayOfYear());
              break;
            case VARCHAR:
              ((VarCharVector) vector).setSafe(rowCount, values[i].getBytes());
              break;
            default:
          }
        }
        rowCount++;
      }
      root.setRowCount(rowCount);

      // Write Arrow vectors to Parquet file
      File file = new File(outputDir, table.getTableName() + ".arrow");
      try (FileOutputStream out = new FileOutputStream(file);
           ArrowFileWriter writer = new ArrowFileWriter(root, null, Channels.newChannel(out))) {
        writer.start();
        writer.writeBatch();
        writer.end();
      }
    }
  }
}
