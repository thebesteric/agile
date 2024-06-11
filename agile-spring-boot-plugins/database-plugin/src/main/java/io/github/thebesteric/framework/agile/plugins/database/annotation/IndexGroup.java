package io.github.thebesteric.framework.agile.plugins.database.annotation;

import java.lang.annotation.*;

/**
 * 索引组
 *
 * @author wangweijun
 * @since 2024/5/28 11:10
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(IndexGroups.class)
@Documented
public @interface IndexGroup {
    /** 对应数据库字段名称，组成联合索引 */
    String[] columns();
}
