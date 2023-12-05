package net.peng.vulpes.runtime.convertor;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.configuration.ConfigItems;
import net.peng.vulpes.common.utils.ObjectUtils;
import net.peng.vulpes.parser.algebraic.logical.RelalgAggregation;
import net.peng.vulpes.parser.algebraic.logical.RelalgJoin;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;
import net.peng.vulpes.parser.algebraic.logical.RelalgProjection;
import net.peng.vulpes.parser.algebraic.logical.RelalgScan;
import net.peng.vulpes.parser.algebraic.logical.RelalgSelection;
import net.peng.vulpes.parser.visitor.RelalgNodeVisitor;
import net.peng.vulpes.runtime.DataFetcher;
import net.peng.vulpes.runtime.exchange.ExchangeService;
import net.peng.vulpes.runtime.physics.DataOutputExecutorNode;
import net.peng.vulpes.runtime.physics.DataSenderExecutorNode;
import net.peng.vulpes.runtime.physics.PrintExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExchangeInputExecutorNode;
import net.peng.vulpes.runtime.physics.base.ExecutorNode;
import net.peng.vulpes.runtime.physics.base.OutputExecutorNode;

/**
 * Description of ConvertVisitor.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class PhysicsNodeBuilder extends RelalgNodeVisitor {

  @Getter
  private int chainIndex;

  @Getter
  private final Map<Integer, Deque<ConvertNode>> relalgNodeChains = new HashMap<>();

  private final Config config;

  public PhysicsNodeBuilder(Config config) {
    this.config = config;
    this.chainIndex = 0;
  }

  public PhysicsNodeBuilder(Config config, Integer rootIndex) {
    this.config = config;
    this.chainIndex = rootIndex;
  }

  @Override
  public RelalgNode visit(RelalgScan relalgScan) {
    getOrCreateChain().addFirst(new ConvertNode(relalgScan, PhysicsConvertorRules.TABLE_SCAN,
            chainIndex, null));
    return super.visit(relalgScan);
  }

  @Override
  public RelalgNode visit(RelalgSelection relalgSelection) {
    getOrCreateChain().addFirst(new ConvertNode(relalgSelection,
            PhysicsConvertorRules.SELECTION, chainIndex, null));
    return super.visit(relalgSelection);
  }

  @Override
  public RelalgNode visit(RelalgProjection relalgProjection) {
    getOrCreateChain().addFirst(new ConvertNode(relalgProjection,
            PhysicsConvertorRules.PROJECTION, chainIndex, null));
    return super.visit(relalgProjection);
  }

  @Override
  public RelalgNode visit(RelalgAggregation relalgAggregation) {
    getOrCreateChain().addFirst(new ConvertNode(relalgAggregation,
            PhysicsConvertorRules.AGGREGATE, chainIndex, null));
    return super.visit(relalgAggregation);
  }

  @Override
  public RelalgNode visit(RelalgJoin relalgJoin) {
    final int thisIndex = chainIndex;
    final int leftIndex = buildChildNode(relalgJoin.getLeft());
    final int rightIndex = buildChildNode(relalgJoin.getRight());
    getOrCreateChain(thisIndex).addFirst(new ConvertNode(relalgJoin,
            PhysicsConvertorRules.JOIN, thisIndex, ImmutableList.of(leftIndex, rightIndex)));
    // 不继续遍历了.
    return relalgJoin;
  }

  private Integer buildChildNode(RelalgNode childNode) {
    PhysicsNodeBuilder leftNodeBuilder = new PhysicsNodeBuilder(config, chainIndex + 1);
    childNode.accept(leftNodeBuilder);
    relalgNodeChains.putAll(leftNodeBuilder.getRelalgNodeChains());
    chainIndex = leftNodeBuilder.getChainIndex();
    return chainIndex;
  }

  /**
   * 构建物理执行节点.
   */
  public List<ExecutorNode> build(RelalgNode relalgNode) {
    relalgNode.accept(this);
    Map<Integer, ExchangeService> shuffleDataMap = new HashMap<>();
    List<ExecutorNode> pipelines = new ArrayList<>();
    for (int i = chainIndex; i >= 0; i--) {
      if (relalgNodeChains.containsKey(i)) {
        pipelines.add(buildChain(i, relalgNodeChains.get(i), shuffleDataMap));
      }
    }
    return pipelines;
  }

  private ExecutorNode buildChain(Integer chainId, Deque<ConvertNode> relalgNodeChain,
                                  Map<Integer, ExchangeService> shuffleDataMap) {
    ExecutorNode executorNode = buildSinkNode(chainId, relalgNodeChain.getLast().relalgNode);
    if (executorNode instanceof DataSenderExecutorNode) {
      shuffleDataMap.put(chainId, ((DataSenderExecutorNode) executorNode).getExchangeService());
    }
    while (!relalgNodeChain.isEmpty()) {
      ConvertNode convertNode = relalgNodeChain.removeLast();
      executorNode = convertNode.physicsConvertor.convert(convertNode.relalgNode, config,
              executorNode);
      if (executorNode instanceof ExchangeInputExecutorNode) {
        ((ExchangeInputExecutorNode) executorNode).setExchangeServiceList(
                convertNode.previousChainId.stream().map(shuffleDataMap::get).toList());
      }
    }
    return executorNode;
  }

  private OutputExecutorNode buildSinkNode(Integer chainId, RelalgNode relalgNode) {
    if (chainId == 0) {
      return new DataOutputExecutorNode(relalgNode.getRowHeader());
    }
    ExchangeService exchangeService = ObjectUtils.reflectionNewInstance(
            config.get(ConfigItems.DATA_EXCHANGE_CLASS), ExchangeService.class, ImmutableList.of());
    return new DataSenderExecutorNode(relalgNode.getRowHeader(), exchangeService);
  }

  private Deque<ConvertNode> getOrCreateChain(Integer thisIndex) {
    if (relalgNodeChains.containsKey(thisIndex)) {
      return relalgNodeChains.get(thisIndex);
    }
    Deque<ConvertNode> relalgNodeDeque = new ArrayDeque<>();
    relalgNodeChains.put(thisIndex, relalgNodeDeque);
    return relalgNodeDeque;
  }

  private Deque<ConvertNode> getOrCreateChain() {
    return getOrCreateChain(chainIndex);
  }

  /**
   * 存储代数表达式的节点.
   */
  public record ConvertNode(RelalgNode relalgNode, PhysicsConvertor physicsConvertor, int chainId,
                            List<Integer> previousChainId) {
  }
}
