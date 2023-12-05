package net.peng.vulpes.runtime.convertor;

/**
 * Description of PhysicsConvertorRules.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class PhysicsConvertorRules {

  public static final PhysicsConvertor TABLE_SCAN = TableScanToFileScan.CONVERTOR;

  public static final PhysicsConvertor SELECTION = SelectionToSearchExecutor.CONVERTOR;

  public static final PhysicsConvertor PROJECTION = ProjectionToExecutorNode.CONVERTOR;

  public static final PhysicsConvertor AGGREGATE = AggregateToExecutorNode.CONVERTOR;

  public static final PhysicsConvertor JOIN = JoinToExecutorNode.CONVERTOR;
}
