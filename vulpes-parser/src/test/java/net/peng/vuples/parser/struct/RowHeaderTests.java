package net.peng.vuples.parser.struct;

import java.util.ArrayList;
import java.util.List;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description of RowHeaderTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/14
 */
public class RowHeaderTests {

  @Test
  public void internalAliasIndexOfTest() {
    List<ColumnInfo> columnInfoList1 = new ArrayList<>();
    columnInfoList1.add(ColumnInfo.builder().name("a").dataType(new VarcharType()).build());
    columnInfoList1.add(ColumnInfo.builder().name("b").dataType(new VarcharType()).build());
    List<ColumnInfo> columnInfoList2 = new ArrayList<>();
    columnInfoList2.add(ColumnInfo.builder().name("c").dataType(new VarcharType()).build());
    columnInfoList2.add(ColumnInfo.builder().name("d").dataType(new VarcharType()).build());
    RowHeader rowHeader1 =
            new RowHeader(columnInfoList1, "t1");
    RowHeader rowHeader2 =
            new RowHeader(columnInfoList2, "t2");
    rowHeader1.addRowHeader(rowHeader2, "t2");
    Assert.assertEquals("2", rowHeader1.indexOf("t2", "c").toString());
    Assert.assertEquals("1", rowHeader1.aliasInternalIndexOf("t2", "d").toString());
    Assert.assertEquals("1", rowHeader1.aliasIndexOf("t2", "c").toString());
  }
}
