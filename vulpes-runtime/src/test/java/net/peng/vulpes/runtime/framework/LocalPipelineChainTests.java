package net.peng.vulpes.runtime.framework;

import com.google.common.collect.ImmutableList;
import java.util.Properties;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.PhysicsNodeTestBase;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.framework.local.LocalPipelineChain;
import net.peng.vulpes.runtime.memory.MemorySpace;
import net.peng.vulpes.runtime.physics.FileScanExecutorNode;
import net.peng.vulpes.runtime.physics.PrintExecutorNode;
import net.peng.vulpes.runtime.physics.SearchExecutorNode;
import org.apache.arrow.dataset.file.FileFormat;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.junit.Test;

/**
 * Description of LocalPipelineChain.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
public class LocalPipelineChainTests extends PhysicsNodeTestBase {

  @Test
  public void simpleLocalPipelineTest() {
    // 配置环境准备
    Properties properties = new Properties();
    properties.put(ConfigItems.FILE_READ_ROW_BATCH_SIZE.name(), 3L);
    Config config = new Config(properties);
    BufferAllocator allocator = new RootAllocator();
    MemorySpace memorySpace = MemorySpace.builder().allocator(allocator).build();
    RowHeader rowHeader = getTableRowHeader();
    // 输出节点
    PrintExecutorNode printExecutorNode = new PrintExecutorNode(rowHeader);
    // 过滤节点
    SearchExecutorNode searchExecutorNode = new SearchExecutorNode(printExecutorNode, "age", 2,
            ImmutableList.of(24, 18), rowHeader, rowHeader);
    // 扫描节点
    FileReader fileReader =  genFileReader("data/table1.csv", FileFormat.CSV, config);
    FileScanExecutorNode fileScanExecutorNode = new FileScanExecutorNode(searchExecutorNode,
            fileReader, rowHeader);
    // 初始化流水线
    LocalPipelineChain localPipelineChain = new LocalPipelineChain(fileScanExecutorNode,
            memorySpace, config);
    localPipelineChain.execute();
    allocator.close();
  }
}
