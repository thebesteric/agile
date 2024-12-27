package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.DataValidationException;

/**
 * o.github.thebesteric.framework.agile.commons.test.ProcessorTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-27 15:12:20
 */
class ProcessorTest {

    @Test
    void test() {
        Assertions.assertThrows(RuntimeException.class, () -> Processor.prepare(String.class)
                .start(() -> "hello world")
                .validate(s -> {
                    throw new DataValidationException("s.length() > 5");
                })
                .next(() -> {
                    return 1L;
                })
                .complete(s -> {
                    System.out.println(s);
                }));
    }

}
