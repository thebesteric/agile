package io.github.thebesteric.framework.agile.plugins.database.annotation;

import lombok.Getter;

import java.lang.annotation.*;

/**
 * 实体类对应字段
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 10:18:26
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EntityColumn {
    /** 对应字段名称，默认属性名的 snack_case 格式 */
    String name() default "";

    /** 类型 */
    Type type() default Type.DETERMINE;

    /** 长度 */
    int length() default -1;

    /** 精读 */
    int precision() default 0;

    /** 主键 */
    boolean primary() default false;

    /** 唯一键 */
    boolean unique() default false;

    /** 注释 */
    String comment() default "";

    /** 是否无符号 */
    boolean unsigned() default false;

    /** 是否为空 */
    boolean nullable() default true;

    /** 默认值 */
    String defaultExpression() default "";

    /** 是否自增 */
    boolean autoIncrement() default false;

    /** 需要更新的表字段 */
    String forUpdate() default "";

    /** 是否是数据库字段 */
    boolean exist() default true;

    @Getter
    enum Type {
        DETERMINE("DETERMINE", false, false, false),

        BOOLEAN("TINYINT", true, false, false),
        TINY_INT("TINYINT", true, false, false),
        INT("INT", true, false, false),
        SMALL_INT("SMALLINT", true, false, false),
        MEDIUM_INT("MEDIUMINT", true, false, false),
        BIG_INT("BIGINT", true, false, false),

        FLOAT("FLOAT", true, true, true),
        DOUBLE("DOUBLE", true, true, true),
        DECIMAL("DECIMAL", true, true, true),

        VARCHAR("VARCHAR", false, true, false),
        TEXT("TEXT", false, false, false),
        BLOB("BLOB", false, false, false),
        JSON("JSON", false, false, false),

        DATE("DATE", false, false, false),
        DATETIME("DATETIME", false, false, false),
        TIMESTAMP("TIMESTAMP", false, false, false);

        /** 字段类型名称 */
        private final String jdbcType;
        /** 是否支持符号 */
        private final boolean supportSign;
        /** 是否支持长度 */
        private final boolean supportLength;
        /** 是否支持精度 */
        private final boolean supportPrecision;

        Type(String jdbcType, boolean supportSign, boolean supportLength, boolean supportPrecision) {
            this.jdbcType = jdbcType;
            this.supportSign = supportSign;
            this.supportLength = supportLength;
            this.supportPrecision = supportPrecision;
        }
    }

}
