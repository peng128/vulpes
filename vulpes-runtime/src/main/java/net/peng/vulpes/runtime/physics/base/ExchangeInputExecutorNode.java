package net.peng.vulpes.runtime.physics.base;

import java.util.List;
import lombok.Setter;
import net.peng.vulpes.parser.algebraic.struct.RowHeader;
import net.peng.vulpes.runtime.exchange.ExchangeService;

/**
 * Description of ExchangeInputExecutorNode.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/16
 */
public abstract class ExchangeInputExecutorNode extends InputExecutorNode {

  protected List<ExchangeService> exchangeServiceList;

  public ExchangeInputExecutorNode(ExecutorNode next, RowHeader outputRowHeader) {
    super(next, outputRowHeader);
  }

  public void setExchangeServiceList(List<ExchangeService> exchangeServiceList) {
    this.exchangeServiceList = exchangeServiceList;
  }
}
