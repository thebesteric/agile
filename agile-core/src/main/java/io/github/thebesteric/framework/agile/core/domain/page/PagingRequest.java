package io.github.thebesteric.framework.agile.core.domain.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页请求基类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-19 17:37:52
 */
@Getter
@Setter
public abstract class PagingRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 2067724472495543463L;

    @Schema(description = "每页显示数量，，默认：10")
    protected long size = 10;

    @Schema(description = "当前页，默认：1")
    protected long current = 1;

    public void setSize(long size) {
        if (size < 0) {
            this.size = Integer.MAX_VALUE;
        } else {
            this.size = size;
        }
    }

    public void setCurrent(long current) {
        if (current <= 0) {
            this.current = 1;
        } else {
            this.current = current;
        }
    }

    public <T> IPage<T> getPage(Class<T> recordType) {
        return new Page<>(this.current, this.size);
    }

    /**
     * 设置不分页
     *
     * @author wangweijun
     * @since 2024/5/31 11:16
     */
    public void unlimited() {
        this.current = 1;
        this.size = Integer.MAX_VALUE;
    }
}
