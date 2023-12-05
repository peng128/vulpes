package net.peng.vuples.test.kit;

import java.util.Properties;
import net.peng.vulpes.common.configuration.Config;
import net.peng.vulpes.common.session.SessionManager;
import net.peng.vulpes.parser.Parser;
import net.peng.vulpes.parser.algebraic.logical.RelalgNode;

/**
 * Description of ParserTools.
 *
 * @author peng
 * @version 1.0
 * @since 2023/11/2
 */
public class ParserTools {

  /**
   * 从文件读取关系表达式.
   */
  public static RelalgNode parse(String inputSqlFileName) {
    return Parser.parse(ResourceFileUtils.getText("parse-test/" + inputSqlFileName), null,
            SessionManager.builder().config(new Config(new Properties()))
                    .currentCatalog("embedded-catalog").currentSchema("test").build());
  }
}
