package net.peng.vulpes.parser.algebraic.expression;

/**
 * Description of FullColumnRef.
 * 这里为了标识使用了全部的列，当读取过表元数据后，这个引用将被转化.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/7
 */
public class FullColumnRef extends RelalgExpr {
  @Override
  public String toString() {
    return "*";
  }
}
