package net.peng.vuples.test.kit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Description of ResourceFileUtils.
 *
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public class ResourceFileUtils {

  /**
   * 从资源文件夹下读取文件.
   *
   * @param fileName 文件名称
   * @return 文件内容
   */
  public static String getText(String fileName) {
    try {
      // 获取资源文件的输入流
      InputStream inputStream =
              ResourceFileUtils.class.getClassLoader().getResourceAsStream(fileName);
      if (inputStream != null) {
        // 使用 BufferedReader 逐行读取输入流中的内容并构建字符串
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
                StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append("\n");
        }
        reader.close();
        // 输出字符串内容
        return content.toString();
      } else {
        throw new RuntimeException("无法找到资源文件: " + fileName);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
