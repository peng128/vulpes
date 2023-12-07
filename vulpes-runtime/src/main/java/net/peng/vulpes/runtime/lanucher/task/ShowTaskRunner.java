package net.peng.vulpes.runtime.lanucher.task;

import java.util.List;
import net.peng.vulpes.catalog.manager.CatalogManager;
import net.peng.vulpes.catalog.manager.CatalogManagerFactory;
import net.peng.vulpes.common.exception.ComputeException;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.parser.algebraic.RelationAlgebraic;
import net.peng.vulpes.parser.algebraic.meta.ShowCatalogMetaNode;
import net.peng.vulpes.parser.algebraic.meta.ShowMetaNode;
import net.peng.vulpes.parser.algebraic.meta.ShowSchemaMetaNode;
import net.peng.vulpes.parser.algebraic.meta.ShowTableMetaNode;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.OutputSegment;

/**
 * 用于展示列表的执行器.
 */
public class ShowTaskRunner implements TaskRunner {
  @Override
  public OutputSegment run(RelationAlgebraic relationAlgebraic, SessionManager sessionManager) {
    if (!(relationAlgebraic instanceof ShowMetaNode showMetaNode)) {
      throw new ComputeException("不能执行show请求:%s", relationAlgebraic);
    }
    final List<ColumnInfo> columns = List.of(ColumnInfo.builder()
        .name(showMetaNode.showColumnName())
        .dataType(new VarcharType()).build());
    CatalogManager catalogManager = CatalogManagerFactory.newInstance(sessionManager.getConfig());
    if (showMetaNode instanceof ShowCatalogMetaNode) {
      final List<Row> rows = catalogManager.getCatalogLoader().getCatalogNames().stream()
          .map(name -> new Row(List.of(name))).toList();
      return new OutputSegment(rows, columns);
    } else if (showMetaNode instanceof ShowSchemaMetaNode) {
      final List<Row> rows = catalogManager.getCatalogLoader()
          .getCatalog(sessionManager.getCurrentCatalog()).getSchemas().stream()
          .map(name -> new Row(List.of(name))).toList();
      return new OutputSegment(rows, columns);
    } else if (showMetaNode instanceof ShowTableMetaNode) {
      final List<Row> rows = catalogManager.getCatalogLoader()
          .getCatalog(sessionManager.getCurrentCatalog())
          .getTableNames(sessionManager.getCurrentSchema()).stream()
          .map(name -> new Row(List.of(name))).toList();
      return new OutputSegment(rows, columns);
    }
    throw new ComputeException("找不到操作的对应执行方法[%s].", relationAlgebraic);
  }
}
