package io.github.thebesteric.framework.agile.plugins.sensitive.filter.exception;

import io.github.thebesteric.framework.agile.commons.util.MessageFormatUtils;
import lombok.Getter;

import java.io.Serial;
import java.util.List;

/**
 * SensitiveCheckException
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-09 09:24:20
 */
@Getter
public class SensitiveException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -2036396192943042257L;

    private final List<String> sensitiveWords;

    public SensitiveException(List<String> sensitiveWords) {
        this(sensitiveWords, "Sensitive check exception");
    }

    public SensitiveException(List<String> sensitiveWords, String message, Object... params) {
        super(MessageFormatUtils.format(message, params));
        this.sensitiveWords = sensitiveWords;
    }
}
