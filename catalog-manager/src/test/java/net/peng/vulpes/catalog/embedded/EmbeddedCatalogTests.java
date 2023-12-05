package net.peng.vulpes.catalog.embedded;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.peng.vulpes.catalog.Catalog;
import net.peng.vulpes.catalog.table.TableMeta;
import net.peng.vulpes.common.model.DataFormat;
import net.peng.vulpes.common.model.TableIdentifier;
import net.peng.vulpes.common.type.DataType;
import net.peng.vulpes.common.type.IntType;
import net.peng.vulpes.common.type.VarcharType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of EmbeddedCatalogTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/15
 */
public class EmbeddedCatalogTests {

  private final String catalogTestFileBase = EmbeddedCatalogTests.class.getClassLoader()
          .getResource("embedded-catalog").getPath();

  @Test
  public void getTableTest() {
    Catalog catalog = new EmbeddedCatalog(catalogTestFileBase);
    TableMeta tableMeta = catalog.getTable(TableIdentifier.create(
            Arrays.asList("embedded-catalog", "test", "table1")));
    List<String> fileNames = Arrays.asList("col1", "col2", "col3", "col4");
    List<DataType> types = Arrays.asList(new VarcharType(), new VarcharType(), new IntType(),
            new IntType());
    List<String> dataFiles = ImmutableList.of("1", "2");
    DataFormat dataFormat = DataFormat.CSV;
    Assert.assertEquals(new EmbeddedTableMeta(fileNames, types, dataFiles, dataFormat), tableMeta);
  }
}
