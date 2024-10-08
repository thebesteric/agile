package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页对象
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-11 09:57:58
 */
@Data
public class Page<T> {

    // 记录
    private List<T> records;

    // 总记录数
    private long total;

    // 每页显示数量
    private long size;

    // 当前页
    private long current;

    // 总页数
    private long pages;

    // 扩展字段
    private Map<String, Object> extension;

    public Page(long current, long size, long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records == null ? new ArrayList<>() : records;
        this.pages = this.calcPages();
    }

    private long calcPages() {
        if (this.getSize() == 0L) {
            return 0L;
        } else {
            long totalPage = this.getTotal() / this.getSize();
            if (this.getTotal() % this.getSize() != 0L) {
                ++totalPage;
            }
            return totalPage;
        }
    }

    public boolean hasPrevious() {
        return this.current > 1L;
    }

    public boolean hasNext() {
        return this.current < this.pages;
    }

    public static <T extends Serializable> Page<T> of(long current, long size, long total, List<T> records) {
        return new Page<>(current, size, total, records);
    }

    public static <T extends Serializable> Page<T> of(List<T> records) {
        return new Page<>(1, records.size(), records.size(), records);
    }

    public Page<T> extension(String key, Object value) {
        if (this.extension == null) {
            this.extension = new HashMap<>();
        }
        this.extension.put(key, value);
        return this;
    }

    public Page<T> extension(Map<String, Object> extension) {
        this.extension = extension;
        return this;
    }

}
