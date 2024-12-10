package io.github.thebesteric.framework.agile.test.entity;


import io.github.thebesteric.framework.agile.plugins.database.core.annotation.*;

/**
 * Bar
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-23 10:26:12
 */
@EntityClass(value = "bar", comment = "The bar", schemas = "test")
@Unique(column = "a")
@Unique(column = "b")
@UniqueGroup(columns = {"a", "b"})
@Index(column = "d")
@Index(column = "e")
@IndexGroup(columns = {"e", "d"})
public class Bar {

    @EntityColumn(reference = @Reference(targetEntityClass = Foo.class, targetColumn = "id"))
    private Integer fooId;

    @EntityColumn(length = 32, uniqueGroup = "a_b")
    private String a;

    @EntityColumn(length = 32, uniqueGroup = "a_b")
    private String b;

    @EntityColumn(length = 32, unique = true)
    private String c;

    @EntityColumn(length = 32, indexGroup = "d_e")
    private String d;

    @EntityColumn(length = 32, indexGroup = "d_e")
    private String e;

    @EntityColumn(length = 32, indexGroup = "f_g", indexGroupSort = 2)
    private String f;

    @EntityColumn(length = 32, indexGroup = "f_g", indexGroupSort = 1)
    private String g;

    @EntityColumn(length = 32, index = true)
    private String h;

    @EntityColumn(length = 32, uniqueGroup = "i_j")
    private String i;

    @EntityColumn(length = 32, uniqueGroup = "i_j")
    private String j;

    private Season season;

    @EntityColumn(type = EntityColumn.Type.TINY_BLOB)
    private Byte[] bytes;

}
