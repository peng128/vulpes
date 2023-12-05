package net.peng.vulpes.common.model.context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.peng.vulpes.common.exception.ContextException;
import net.peng.vulpes.common.utils.ObjectUtils;

/**
 * Description of WrapContext.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/16
 */
public class WrapContext implements Context {

  private final Object object;

  protected WrapContext(Object object) {
    this.object = object;
  }

  @Override
  public <T> T get(Class<T> clazz) {
    if (ObjectUtils.isNull(object)) {
      throw new ContextException("上下文为空.");
    }
    if (!clazz.isInstance(object)) {
      throw new ContextException("找不到对应类型的上下文[%s].", clazz.getName());
    }
    return clazz.cast(object);
  }

  /**
   * 填充上下文实现的类.
   */
  public static Context of(Object object) {
    return new WrapContext(object);
  }

  /**
   * 可以存放多个{@link Context}.
   */
  public static class ChainContext implements Context {

    private final List<Context> contexts;

    protected ChainContext(List<Context> contexts) {
      this.contexts = contexts;
    }

    @Override
    public <T> T get(Class<T> clazz) {
      List<T> output = contexts.stream().map(context -> context.get(clazz))
              .filter(ObjectUtils::isNotNull).collect(Collectors.toList());
      if (output.size() > 1) {
        throw new ContextException("找到两个以上[%s]类型的上下文.[%s]", clazz.getName(), output);
      }
      if (output.isEmpty()) {
        throw new ContextException("没有找到对应类型[%s]的上下文.", clazz.getName());
      }
      return output.get(0);
    }
  }

  public static Context of(Context... contexts) {
    return new ChainContext(Arrays.stream(contexts).collect(Collectors.toList()));
  }
}
