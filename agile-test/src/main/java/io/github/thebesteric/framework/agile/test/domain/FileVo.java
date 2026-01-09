package io.github.thebesteric.framework.agile.test.domain;

import lombok.Data;
import org.springframework.core.io.Resource;

/**
 * FileVo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-09 11:12:59
 */
@Data
public class FileVo {

    private String fileName;
    private Resource resource;
}
