package io.github.thebesteric.framework.agile.plugins.database.core.annotation;

import java.lang.annotation.*;

/**
 * 唯一索引组
 *
 * @author wangweijun
 * @since 2024/5/13 09:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UniqueGroups.class)
@Documented
public @interface UniqueGroup {
    /** 对应数据库字段名称，组成联合唯一 */
    String[] columns();
}
