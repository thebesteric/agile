package io.github.thebesteric.framework.agile.plugins.database.annotation;

import java.lang.annotation.*;

/**
 * 索引
 *
 * @author wangweijun
 * @since 2024/5/28 11:09
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Indexes.class)
@Documented
public @interface Index {
    /** 对应数据库字段名称 */
    String column();
}
