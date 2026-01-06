package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.exception.DataExistsException;
import io.github.thebesteric.framework.agile.commons.exception.DataValidationException;
import io.github.thebesteric.framework.agile.commons.util.DataValidator;
import io.github.thebesteric.framework.agile.commons.util.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertThrows(RuntimeException.class, () ->
                Processor.prepare(DataValidator.ExceptionThrowStrategy.COLLECT)
                        .start(() -> "hello world")
                        .validate(s -> {
                            throw new DataValidationException("s.length() > 5");
                        })
                        .next(() -> {
                            return 1L;
                        })
                        .interim(() -> {

                        })
                        .interim((t) -> {
                            System.out.println(t);
                        })
                        .complete((s, exceptions) -> {
                            System.out.println("result = " + s);
                            System.out.println("exceptions = " + exceptions);
                            if (exceptions.get(0) instanceof DataExistsException dataExistsException) {
                                throw dataExistsException;
                            }
                            return 2L;
                        }));
    }

}
