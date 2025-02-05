package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.ConditionMatcher;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ConditionMatcherTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-02-05 14:41:23
 */
class ConditionMatcherTest {

    @Test
    void test() throws NoSuchMethodException {
        Address address = new Address("安徽合肥");
        Request request = new Request(1, address);
        Method method = ConditionMatcherTest.class.getMethod("test", Request.class);
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[]{request};
        System.out.println(ConditionMatcher.parseExpression("abc + :: + #request.id + :: + #request.address.name + :: + bcd", parameters, arguments));
    }

    public void test(Request request) {
        System.out.println(request);
    }

    @Data
    @AllArgsConstructor
    class Address {
        String name;
    }

    @Data
    @AllArgsConstructor
    class Request {
        int id;
        Address address;
    }

}
