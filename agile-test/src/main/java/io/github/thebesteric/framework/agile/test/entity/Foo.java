package io.github.thebesteric.framework.agile.test.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityColumn;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Foo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 15:24:30
 */
@TableName("foo")
public class Foo extends BaseEntity {

    @EntityColumn(length = 32, unique = true, nullable = false, forUpdate = "hello", defaultExpression = "'foo'")
    private String name;

    @EntityColumn(name = "t_phone", unique = true, nullable = false, defaultExpression = "18", comment = "电话", unsigned = true)
    private Integer age;

    @EntityColumn(unique = true, defaultExpression = "'test'")
    private String address;

    @EntityColumn(length = 10, precision = 3, unique = true)
    private BigDecimal amount;

    @EntityColumn(nullable = false, type = EntityColumn.Type.SMALL_INT, unsigned = true)
    private Season season;

    @EntityColumn(length = 10, precision = 2)
    private Float state;

    @EntityColumn(type = EntityColumn.Type.DATETIME, defaultExpression = "now()")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("t_test")
    @EntityColumn(length = 64, nullable = false)
    private String test;

    @EntityColumn(nullable = false, defaultExpression = "0")
    private boolean deleted;

    @EntityColumn(length = 64, nullable = false, uniqueGroup = "a_b")
    private String a;

    @EntityColumn(length = 64, nullable = false, uniqueGroup = "a_b")
    private String b;

    @EntityColumn(length = 64, index = true, indexGroup = "c_e_d", indexGroupSort = 1)
    private String c;

    @EntityColumn(length = 64, indexGroup = "c_e_d", indexGroupSort = 3)
    private String d;

    @EntityColumn(length = 64, indexGroup = "c_e_d", indexGroupSort = 2)
    private String e;
}
