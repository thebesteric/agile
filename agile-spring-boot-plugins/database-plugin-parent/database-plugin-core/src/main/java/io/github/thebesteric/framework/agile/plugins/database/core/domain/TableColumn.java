package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数据库列信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-21 13:19:14
 */
@Data
@Accessors(chain = true)
public class TableColumn {
    private String tableCat;
    private String tableSchema;
    private String tableName;
    private String columnName;
    private Integer dataType;
    private String typeName;
    private Integer columnSize;
    private Integer decimalDigits;
    private Integer numPrecisionRadix;
    private boolean nullable;
    private String remarks;
}
