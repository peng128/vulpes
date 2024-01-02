package net.peng.vulpes.benchmark;

import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Description of TpchGeneratorTests.
 *
 * @author peng
 * @version 1.0
 * @since 2023/12/14
 */
public class TpchGeneratorTests {

  @Ignore
  @Test
  public void arrowGenTest() throws IOException {
    // Set the scale factor and output directory
    double scaleFactor = 0.01;
    System.out.println(System.getProperty("user.dir"));
    String outputDir = "../tpch/";
    TpchDataSetGenerator.genTpchArrowData(scaleFactor, outputDir);
  }
}
