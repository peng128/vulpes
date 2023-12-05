package net.peng.vulpes.launcher;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.common.type.VarcharType;
import net.peng.vulpes.launcher.utils.ByteUtils;
import net.peng.vulpes.launcher.utils.MysqlClientUtils;
import net.peng.vulpes.parser.algebraic.struct.ColumnInfo;
import net.peng.vulpes.parser.utils.DataTypeEstimateUtils;
import net.peng.vulpes.runtime.lanucher.StatementRunner;
import net.peng.vulpes.runtime.struct.Row;
import net.peng.vulpes.runtime.struct.data.OutputSegment;
import net.peng.vuples.jdbc.mysql.ResultSetBuilder;
import net.peng.vuples.jdbc.mysql.execute.StatementExecutor;
import net.peng.vuples.jdbc.mysql.mycat.ErrorPacket;

/**
 * Description of DefaultStatementRunner.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/20
 */
@Slf4j
public class DefaultStatementExecutor implements StatementExecutor {
  StatementRunner statementRunner = new StatementRunner();

  @Override
  public byte[] execute(String statement, SessionManager sessionManager) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    OutputSegment resultSegment;
    try {
      resultSegment = statementRunner.run(statement, sessionManager);
    } catch (Exception e) {
      String msg = String.format("语句执行错误: %s \n原因为: %s \n跟踪堆栈为: %s", statement, e.getMessage(),
              Arrays.toString(e.getStackTrace()));
      log.warn(msg);
      return ErrorPacket.builder().errno(123)
              .message(msg.getBytes()).build().writeToBytes().array();
    }
    List<Integer> types =
            resultSegment.getColumns().stream().map(columnInfo ->
                    MysqlClientUtils.dataTypeConvert(columnInfo.getDataType())).toList();
    List<List<byte[]>> data =
            resultSegment.getRows().stream().map(row ->
                    row.getData().stream().map(ByteUtils::getBytes).toList()).toList();
    ResultSetBuilder resultSetBuilder = new ResultSetBuilder(resultSegment.getColumns()
            .stream().map(ColumnInfo::getName).toList(), types, data);
    log.debug("全部计算用时: {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    return resultSetBuilder.encode().array();
  }
}
