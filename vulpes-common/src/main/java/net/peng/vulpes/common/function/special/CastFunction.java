package net.peng.vulpes.common.function.special;

import net.peng.vulpes.common.function.Function;
import net.peng.vulpes.common.function.FunctionName;

/**
 * Description of CastFunction.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/29
 */
@FunctionName(name = "CAST", kind = 2)
public class CastFunction implements Function {

  //TODO: 之后需要完善CAST.
  public Object eval(Object a, String b) {
    return a;
  }
}
