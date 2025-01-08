package io.github.thebesteric.framework.agile.test.sensitive;

import io.github.thebesteric.framework.agile.commons.util.MessageUtils;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.AgileSensitiveFilter;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.config.AgileSensitiveFilterProperties;
import io.github.thebesteric.framework.agile.plugins.sensitive.filter.domain.SensitiveFilterResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * SenstiveTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-08 15:21:16
 */
@SpringBootTest
class SensitiveTest {

    @Resource
    AgileSensitiveFilter agileSensitiveFilter;

    private static final String MESSAGE = "你好，敏感词嫖xx娼过滤测试，我想去&赌*&-x博";

    @Test
    void singleton() {
        AgileSensitiveFilterProperties properties = new AgileSensitiveFilterProperties();
        properties.setFilePath("asserts/sensitive.txt");
        properties.getSymbols().add('x');
        AgileSensitiveFilter sensitiveFilter = new AgileSensitiveFilter(properties, null);
        sensitiveFilter.init();

        SensitiveFilterResult result = sensitiveFilter.filter(MESSAGE);
        System.out.println("isPass = " + result.isPassed());
        System.out.println("original = " + result.getOriginal());
        System.out.println("result = " + result.getResult());
        System.out.println("placeholder = " + result.getPlaceholder());
        for (SensitiveFilterResult.Sensitive sensitiveWord : result.getSensitiveWords()) {
            System.out.println(MessageUtils.replacePlaceholders("sensitiveWord: start: {}, end: {}, keyword: {}", sensitiveWord.getStart(), sensitiveWord.getEnd(), sensitiveWord.getKeyword()));
        }
        Assertions.assertFalse(result.isPassed());
    }

    @Test
    void test() {
        SensitiveFilterResult result = agileSensitiveFilter.filter(MESSAGE);
        System.out.println("isPass = " + result.isPassed());
        System.out.println("original = " + result.getOriginal());
        System.out.println("result = " + result.getResult());
        System.out.println("placeholder = " + result.getPlaceholder());
        for (SensitiveFilterResult.Sensitive sensitiveWord : result.getSensitiveWords()) {
            System.out.println(MessageUtils.replacePlaceholders("sensitiveWord: start: {}, end: {}, keyword: {}", sensitiveWord.getStart(), sensitiveWord.getEnd(), sensitiveWord.getKeyword()));
        }
        Assertions.assertFalse(result.isPassed());
    }

}
