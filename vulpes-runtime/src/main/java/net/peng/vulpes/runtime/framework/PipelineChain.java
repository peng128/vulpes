package net.peng.vulpes.runtime.framework;

import net.peng.vulpes.runtime.struct.data.Segment;

/**
 * Description of PipelineChain.
 * 用于不做数据交换的一段流水线的执行框架.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/17
 */
public interface PipelineChain {

  /**
   * 执行流水线.
   */
  Segment<?> execute();
}
