package io.github.thebesteric.framework.agile.test.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.IgnoreField;
import io.github.thebesteric.framework.agile.plugins.logger.constant.IgnoreFieldHandleType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

/**
 * FileVo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-09 11:12:59
 */
@Data
@NoArgsConstructor
public class FileVo {

    private String fileName;

    @JsonIgnore
    private Resource resource;

    public FileVo(String fileName, Resource resource) {
        this.fileName = fileName;
        this.resource = resource;
    }
}
