package net.peng.vulpes.runtime.convertor;

import net.peng.vulpes.catalog.table.FileTableMeta;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.runtime.file.FileReader;
import net.peng.vulpes.runtime.physics.FileScanExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import org.apache.arrow.dataset.file.FileFormat;

/**
 * Description of TableScanToFileScan.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/24
 */
public class TableScanToFileScan implements PhysicsConvertor {

  public static final PhysicsConvertor CONVERTOR = new TableScanToFileScan();

  @Override
  public ExecutorNode convert(RelalgNode relalgNode, Config config, ExecutorNode nextNode) {
    RelalgScan relalgScan = (RelalgScan) relalgNode;
    FileTableMeta fileTableMeta = (FileTableMeta) relalgScan.getTableMeta();
    FileReader fileReader =
            new FileReader(fileTableMeta.getDataFiles().stream().map(path ->
                    config.get(ConfigItems.FILE_PATH_PROCESS_WRAPPER).apply(path)).toList(),
                    FileFormat.valueOf(fileTableMeta.getDataFormat().name()), config);
    return new FileScanExecutorNode(nextNode, fileReader, relalgNode.getRowHeader());
  }

  @Override
  public boolean isMatch(RelalgNode relalgNode) {
    if (!(relalgNode instanceof RelalgScan)) {
      return false;
    }
    return ((RelalgScan) relalgNode).getTableMeta() instanceof FileTableMeta;
  }
}
