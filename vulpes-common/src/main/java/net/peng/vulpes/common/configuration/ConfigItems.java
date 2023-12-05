package net.peng.vulpes.common.configuration;

import java.util.function.Function;

/**
 * Description of ConfigItems.
 *
 * @author peng
 * @version 1.0
 * @since 2023/10/16
 */
public class ConfigItems {

  public static final ConfigObject<Integer> JDBC_TPC_PORT =
          ConfigObject.create("jdbc.tpc.port", 13000);

  public static final ConfigObject<Integer> JDBC_WORKER_THREAD_NUM =
          ConfigObject.create("jdbc.worker.thread.num", 10);

  public static final ConfigObject<String> CATALOG_LOADER_CLASS =
          ConfigObject.create("catalog.loader.class",
          "net.peng.vulpes.catalog.loader.StaticCatalogLoader");

  public static final ConfigObject<Long> SESSION_AUTO_INCREMENT = ConfigObject.create(
          "auto_increment_increment", 0L);

  public static final ConfigObject<String> CATALOG_STATIC_CONFIG_FILE_PATH =
          ConfigObject.create("catalog.static.config.file.path", "config/catalog.yaml");

  public static final ConfigObject<Long> FILE_READ_ROW_BATCH_SIZE =
          ConfigObject.create("file.read.row.batch.size", 32768L);

  public static final ConfigObject<String> DATA_EXCHANGE_CLASS =
          ConfigObject.create("data.exchange.class",
                  "net.peng.vulpes.runtime.exchange.MemoryDataFetcher");

  public static final ConfigObject<String> VERSION =
          ConfigObject.create("version_comment",
                  "5.6.29-mycat-1.6.7.5-release-20200428154739");

  public static final ConfigObject<String> COLLATION_SERVER =
          ConfigObject.create("collation_server",
                  "utf8mb4_general_ci");

  //-----------------  测试使用配置 ---------------
  public static final ConfigObject<Function<String, String>> FILE_PATH_PROCESS_WRAPPER =
          ConfigObject.create("file.path.process.wrapper", input -> input);
}
