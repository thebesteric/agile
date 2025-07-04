package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.DataValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * DataValidatorTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-07-04 10:02:53
 */
class DataValidatorTest {

    @Test
    void test() {
        List<Throwable> exceptions = DataValidator.create(DataValidator.ExceptionThrowStrategy.COLLECT)
                .validate(3 == 3, "两个数不能相等")
                .getExceptions();
        System.out.println(exceptions);
    }

}
