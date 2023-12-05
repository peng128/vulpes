package net.peng.vulpes.catalog.loader.config;

import java.util.Map;

/**
 * Description of CatalogItem.
 *
 * @param name 目录名称.
 * @param type 目录类型.
 * @param prop 目录配置属性.
 * @author peng
 * @version 1.0
 * @since 2023/9/13
 */
public record CatalogItem(String name, String type, Map<String, String> prop) {

}
