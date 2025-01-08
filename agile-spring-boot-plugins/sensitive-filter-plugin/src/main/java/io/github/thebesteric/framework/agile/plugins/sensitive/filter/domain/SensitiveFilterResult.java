package io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词过滤结果
 *
 * @author wangweijunSensitiveFilterResult
 * @version v1.0
 * @since 2025-01-08 14:34:19
 */
@Data
public class SensitiveFilterResult {

    /** 原始文本 */
    private String original;
    /** 结果文本 */
    private String result;
    /** 敏感词 */
    private List<Sensitive> sensitiveWords = new ArrayList<>();
    /** 占位符 */
    private String placeholder;

    private SensitiveFilterResult() {
        super();
    }

    public static SensitiveFilterResult empty(String original, String placeholder) {
        return of(original, original, placeholder, new ArrayList<>());
    }

    public static SensitiveFilterResult of(String original, String result, String placeholder, List<Sensitive> sensitiveWords) {
        SensitiveFilterResult sensitiveFilterResult = new SensitiveFilterResult();
        sensitiveFilterResult.original = original;
        sensitiveFilterResult.result = result;
        sensitiveFilterResult.placeholder = placeholder;
        sensitiveFilterResult.sensitiveWords = sensitiveWords;
        return sensitiveFilterResult;
    }

    @Data
    public static class Sensitive {
        /** 开始位置 */
        private int start;
        /** 结束位置 */
        private int end;
        /** 敏感词 */
        private String keyword;

        private Sensitive() {
            super();
        }

        public static Sensitive of(int start, int end, String keyword) {
            Sensitive sensitive = new Sensitive();
            sensitive.start = start;
            sensitive.end = end;
            sensitive.keyword = keyword;
            return sensitive;
        }
    }

}
