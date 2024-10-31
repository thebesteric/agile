package io.github.thebesteric.framework.agile.core.domain.page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于分页的基类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-22 10:50:10
 */
@Getter
@Setter
@NoArgsConstructor
public class PagingResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -5719788927570427145L;

    /** 记录 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 每页显示数量 */
    private long size;

    /** 当前页 */
    private long current;

    /** 总页数 */
    private long pages;

    /** 扩展字段 */
    private transient Map<String, Object> extension;

    public PagingResponse(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
    }

    public long getPages() {
        if (this.getSize() == 0L) {
            return 0L;
        } else {
            long pages = this.getTotal() / this.getSize();
            if (this.getTotal() % this.getSize() != 0L) {
                ++pages;
            }
            return pages;
        }
    }

    public boolean hasPrevious() {
        return this.current > 1L;
    }

    public boolean hasNext() {
        return this.current < this.getPages();
    }

    public static <T> PagingResponse<T> of(long current, long size, long total, List<T> records) {
        return new PagingResponse<>(current, size, total, records);
    }

    public PagingResponse<T> extension(String key, Object value) {
        if (this.extension == null) {
            this.extension = new HashMap<>();
        }
        this.extension.put(key, value);
        return this;
    }

    public PagingResponse<T> extension(Map<String, Object> extension) {
        this.extension = extension;
        return this;
    }
}
