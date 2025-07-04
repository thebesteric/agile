package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * MapWrapperTest
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-07-04 09:58:36
 */
class MapWrapperTest {
    @Data
    public static class User {
        private String name;
        private Integer age;
    }

    @Test
    void test() {
        Map<String, Object> map = MapWrapper.createLambda(User.class)
                .put(User::getName, "张三")
                .put(User::getAge, 18)
                .build();
        System.out.println(map);
    }
}
