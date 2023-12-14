package net.peng.vulpes.catalog.file.arrow;

import java.util.Arrays;
import java.util.List;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.embedded.EmbeddedTableMeta;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.type.BigIntType;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.VarcharType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of FileParquetCatalogTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/11
 */
public class FileArrowCatalogTests {

  private final String catalogTestFileBase = FileArrowCatalogTests.class.getClassLoader()
      .getResource("tpch").getPath();

  @Test
  public void getTableTest() {
    Catalog catalog = new FileArrowCatalog(catalogTestFileBase);
    TableMeta tableMeta = catalog.getTable(TableIdentifier.create(
        Arrays.asList("tpch", "arrow", "customer")));
    List<String> fileNames = Arrays.asList("custkey", "name", "address", "nationkey", "phone",
        "acctbal", "mktsegment", "comment");
    List<DataType> types = Arrays.asList(new VarcharType(), new VarcharType(), new VarcharType(),
        new VarcharType(), new VarcharType(), new BigIntType(), new VarcharType(),
        new VarcharType());
    DataFormat dataFormat = DataFormat.ARROW_IPC;
    Assert.assertEquals(new EmbeddedTableMeta(fileNames, types,
        List.of("tpch/arrow/customer.arrow"), dataFormat), tableMeta);
  }
}
