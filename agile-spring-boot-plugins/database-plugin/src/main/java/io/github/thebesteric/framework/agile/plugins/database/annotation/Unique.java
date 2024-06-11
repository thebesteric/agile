package io.github.thebesteric.framework.agile.plugins.database.annotation;

import java.lang.annotation.*;

/**
 * 唯一索引
 *
 * @author wangweijun
 * @since 2024/5/13 09:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Uniques.class)
@Documented
public @interface Unique {
    /** 对应数据库字段名称 */
    String column();
}
