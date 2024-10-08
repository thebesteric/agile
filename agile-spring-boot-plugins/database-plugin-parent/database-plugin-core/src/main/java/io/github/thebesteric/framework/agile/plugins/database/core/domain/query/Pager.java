package io.github.thebesteric.framework.agile.plugins.database.core.domain.query;

import lombok.Data;

/**
 * Pager
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-01 21:22:57
 */
@Data
public class Pager {
    private Integer page;
    private Integer pageSize;

    private Pager(Integer page, Integer pageSize) {
        this.page = page == null || page <= 1 ? 1 : page;
        this.pageSize = pageSize == null || pageSize < 1 ? Integer.MAX_VALUE : pageSize;
    }

    public static Pager of(Integer page, Integer pageSize) {
        return new Pager(page, pageSize);
    }

    public Integer getOffset() {
        return (this.page - 1) * this.pageSize;
    }
}
