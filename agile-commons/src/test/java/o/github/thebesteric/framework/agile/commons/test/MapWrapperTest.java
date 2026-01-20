package o.github.thebesteric.framework.agile.commons.test;

import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        private String email;
        private Integer age;
        private Integer score;
    }

    @Data
    public static class UserProfile {
        private String firstName;
        private String lastName;
        private String emailAddress;
        private Integer userAge;
        private String companyName;
    }

    @Test
    void testWithoutValueType() {
        Map<String, Object> map = MapWrapper.createLambda(User.class)
                .put(User::getName, "张三")
                .put(User::getAge, 18)
                .build();
        System.out.println(map);
        assertEquals("张三", map.get("name"));
        assertEquals(18, map.get("age"));
    }

    @Test
    void testWithValueType() {
        // 使用 Object 类型，可以放入不同类型的值
        Map<String, Object> map1 = MapWrapper.createLambda(User.class, Object.class)
                .put(User::getName, "张三")
                .put(User::getAge, 18)
                .build();
        System.out.println("map1: " + map1);
        assertEquals("张三", map1.get("name"));
        assertEquals(18, map1.get("age"));

        // 使用 String 类型，只能放入 String 值
        Map<String, String> map2 = MapWrapper.createLambda(User.class, String.class)
                .put(User::getName, "张三")
                .put(User::getEmail, "zhangsan@example.com")
                .build();
        System.out.println("map2: " + map2);
        assertEquals("张三", map2.get("name"));
        assertEquals("zhangsan@example.com", map2.get("email"));

        // 使用 Integer 类型，只能放入 Integer 值
        Map<String, Integer> map3 = MapWrapper.createLambda(User.class, Integer.class)
                .put(User::getAge, 18)
                .put(User::getScore, 95)
                .build();
        System.out.println("map3: " + map3);
        assertEquals(18, map3.get("age"));
        assertEquals(95, map3.get("score"));
    }

    @Test
    void testWithKeyStyle() {
        // 测试 SNAKE_CASE 风格
        Map<String, Object> map1 = MapWrapper.createLambda(User.class, MapWrapper.KeyStyle.SNAKE_CASE)
                .put(User::getName, "李四")
                .put(User::getAge, 25)
                .build();
        System.out.println("SNAKE_CASE map: " + map1);
        assertEquals("李四", map1.get("name"));
        assertEquals(25, map1.get("age"));

        // 测试 CAMEL_CASE 风格
        Map<String, Object> map2 = MapWrapper.createLambda(User.class, MapWrapper.KeyStyle.CAMEL_CASE)
                .put(User::getName, "王五")
                .put(User::getEmail, "wangwu@example.com")
                .build();
        System.out.println("CAMEL_CASE map: " + map2);
        assertEquals("王五", map2.get("name"));
        assertEquals("wangwu@example.com", map2.get("email"));

        // 测试 NONE 风格
        Map<String, Object> map3 = MapWrapper.createLambda(User.class, MapWrapper.KeyStyle.NONE)
                .put(User::getName, "赵六")
                .put(User::getScore, 88)
                .build();
        System.out.println("NONE map: " + map3);
        assertEquals("赵六", map3.get("name"));
        assertEquals(88, map3.get("score"));
    }

    @Test
    void testWithCondition() {
        // 测试条件为 true 的情况
        Map<String, Object> map1 = MapWrapper.createLambda(User.class)
                .put(true, User::getName, "张三")
                .put(true, User::getAge, 18)
                .build();
        assertEquals(2, map1.size());
        assertEquals("张三", map1.get("name"));
        assertEquals(18, map1.get("age"));

        // 测试条件为 false 的情况
        Map<String, Object> map2 = MapWrapper.createLambda(User.class)
                .put(true, User::getName, "李四")
                .put(false, User::getAge, 25)
                .build();
        assertEquals(1, map2.size());
        assertEquals("李四", map2.get("name"));
        assertNull(map2.get("age"));

        // 测试混合条件
        int score = 85;
        Map<String, Object> map3 = MapWrapper.createLambda(User.class)
                .put(User::getName, "王五")
                .put(score >= 60, User::getScore, score)
                .put(score >= 90, User::getEmail, "excellent@example.com")
                .build();
        assertEquals(2, map3.size());
        assertEquals("王五", map3.get("name"));
        assertEquals(85, map3.get("score"));
        assertNull(map3.get("email"));
    }

    @Test
    void testCreate() {
        // 测试基本的 create 方法
        Map<String, Object> map1 = MapWrapper.<User, Object>create()
                .put("name", "张三")
                .put("age", 18)
                .build();
        System.out.println("create() map: " + map1);
        assertEquals("张三", map1.get("name"));
        assertEquals(18, map1.get("age"));

        // 测试带 KeyStyle 的 create 方法
        Map<String, Object> map2 = MapWrapper.<User, Object>create(MapWrapper.KeyStyle.SNAKE_CASE)
                .put("userName", "李四")
                .put("userAge", 25)
                .build();
        System.out.println("create(KeyStyle) map: " + map2);
        assertEquals("李四", map2.get("user_name"));
        assertEquals(25, map2.get("user_age"));
    }

    @Test
    void testWithExistingMap() {
        // 测试使用已存在的 Map
        Map<String, Object> existingMap = new HashMap<>();
        existingMap.put("existingKey", "existingValue");

        Map<String, Object> map = MapWrapper.createLambda(User.class, existingMap)
                .put(User::getName, "张三")
                .put(User::getAge, 18)
                .build();

        System.out.println("With existing map: " + map);
        assertEquals(3, map.size());
        assertEquals("existingValue", map.get("existingKey"));
        assertEquals("张三", map.get("name"));
        assertEquals(18, map.get("age"));
    }

    @Test
    void testPutWithStringKey() {
        // 测试使用字符串作为 key
        Map<String, Object> map1 = MapWrapper.<User, Object>create()
                .put("name", "张三")
                .put("age", 18)
                .put("email", "zhangsan@example.com")
                .build();
        assertEquals(3, map1.size());
        assertEquals("张三", map1.get("name"));
        assertEquals(18, map1.get("age"));
        assertEquals("zhangsan@example.com", map1.get("email"));

        // 测试字符串 key 与条件
        Map<String, Object> map2 = MapWrapper.<User, Object>create()
                .put(true, "name", "李四")
                .put(false, "age", 25)
                .build();
        assertEquals(1, map2.size());
        assertEquals("李四", map2.get("name"));
        assertNull(map2.get("age"));

        // 测试字符串 key 与 KeyStyle
        Map<String, Object> map3 = MapWrapper.<User, Object>create()
                .put("userName", "王五", MapWrapper.KeyStyle.SNAKE_CASE)
                .put("userAge", 30, MapWrapper.KeyStyle.SNAKE_CASE)
                .build();
        assertEquals(2, map3.size());
        assertEquals("王五", map3.get("user_name"));
        assertEquals(30, map3.get("user_age"));
    }

    @Test
    void testEmptyMap() {
        // 测试创建空 Map
        Map<String, Object> emptyMap = MapWrapper.createLambda(User.class).build();
        assertNotNull(emptyMap);
        assertEquals(0, emptyMap.size());
    }

    @Test
    void testChainedPuts() {
        // 测试链式调用
        Map<String, Object> map = MapWrapper.createLambda(User.class)
                .put(User::getName, "张三")
                .put(User::getAge, 18)
                .put(User::getEmail, "zhangsan@example.com")
                .put(User::getScore, 95)
                .build();
        assertEquals(4, map.size());
        assertEquals("张三", map.get("name"));
        assertEquals(18, map.get("age"));
        assertEquals("zhangsan@example.com", map.get("email"));
        assertEquals(95, map.get("score"));
    }

    @Test
    void testNullValues() {
        // 测试 null 值
        Map<String, Object> map = MapWrapper.createLambda(User.class)
                .put(User::getName, "张三")
                .put(User::getAge, null)
                .put(User::getEmail, null)
                .build();
        assertEquals(3, map.size());
        assertEquals("张三", map.get("name"));
        assertNull(map.get("age"));
        assertNull(map.get("email"));
        assertTrue(map.containsKey("age"));
        assertTrue(map.containsKey("email"));
    }

    @Test
    void testValueTypeWithKeyStyle() {
        // 测试 ValueType 与 KeyStyle 结合
        Map<String, String> map = MapWrapper.createLambda(User.class, String.class, MapWrapper.KeyStyle.SNAKE_CASE)
                .put(User::getName, "张三")
                .put(User::getEmail, "zhangsan@example.com")
                .build();
        System.out.println("ValueType with KeyStyle: " + map);
        assertEquals("张三", map.get("name"));
        assertEquals("zhangsan@example.com", map.get("email"));
    }

    @Test
    void testMapType() {
        // 测试 ValueType 与 KeyStyle 结合
        Map<String, String> map = MapWrapper.createLambda(User.class, String.class, new LinkedHashMap<>())
                .put(User::getName, "张三")
                .put(User::getEmail, "zhangsan@example.com")
                .build();
        System.out.println("ValueType with KeyStyle: " + map);
        // 判断 map 是否为 LinkedHashMap
        assertEquals(2, map.size());
        assertInstanceOf(LinkedHashMap.class, map);
    }

    @Test
    void testKeyStyle() {
        System.out.println("=".repeat(80));
        System.out.println("KeyStyle 作用演示");
        System.out.println("=".repeat(80));
        System.out.println();

        // ========================================================================
        // 1. KeyStyle.NONE - 不做任何转换，保持原始字段名
        // ========================================================================
        System.out.println("【1】KeyStyle.NONE - 保持原始字段名");
        System.out.println("-".repeat(80));
        Map<String, Object> map1 = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.NONE)
                .put(UserProfile::getFirstName, "张")
                .put(UserProfile::getLastName, "三")
                .put(UserProfile::getEmailAddress, "zhangsan@example.com")
                .put(UserProfile::getUserAge, 25)
                .put(UserProfile::getCompanyName, "阿里巴巴")
                .build();

        System.out.println("生成的 Map: " + map1);
        System.out.println("键名保持原样:");
        map1.keySet().forEach(key -> System.out.println("  - " + key));
        System.out.println();

        // ========================================================================
        // 2. KeyStyle.SNAKE_CASE - 转换为下划线命名（蛇形命名）
        // ========================================================================
        System.out.println("【2】KeyStyle.SNAKE_CASE - 转换为下划线命名");
        System.out.println("-".repeat(80));
        Map<String, Object> map2 = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.SNAKE_CASE)
                .put(UserProfile::getFirstName, "李")
                .put(UserProfile::getLastName, "四")
                .put(UserProfile::getEmailAddress, "lisi@example.com")
                .put(UserProfile::getUserAge, 30)
                .put(UserProfile::getCompanyName, "腾讯")
                .build();

        System.out.println("生成的 Map: " + map2);
        System.out.println("键名转换为下划线风格:");
        map2.keySet().forEach(key -> System.out.println("  - " + key));
        System.out.println("说明: firstName -> first_name, emailAddress -> email_address");
        System.out.println();

        // ========================================================================
        // 3. KeyStyle.CAMEL_CASE - 转换为驼峰命名
        // ========================================================================
        System.out.println("【3】KeyStyle.CAMEL_CASE - 转换为驼峰命名");
        System.out.println("-".repeat(80));
        Map<String, Object> map3 = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.CAMEL_CASE)
                .put(UserProfile::getFirstName, "王")
                .put(UserProfile::getLastName, "五")
                .put(UserProfile::getEmailAddress, "wangwu@example.com")
                .put(UserProfile::getUserAge, 28)
                .put(UserProfile::getCompanyName, "字节跳动")
                .build();

        System.out.println("生成的 Map: " + map3);
        System.out.println("键名转换为驼峰风格:");
        map3.keySet().forEach(key -> System.out.println("  - " + key));
        System.out.println("说明: 如果原本就是驼峰，保持不变");
        System.out.println();

        // ========================================================================
        // 4. 使用字符串 key 时的 KeyStyle 作用
        // ========================================================================
        System.out.println("【4】使用字符串 key 时的 KeyStyle");
        System.out.println("-".repeat(80));

        // SNAKE_CASE 转换字符串 key
        Map<String, Object> map4 = MapWrapper.<UserProfile, Object>create()
                .put("firstName", "赵", MapWrapper.KeyStyle.SNAKE_CASE)
                .put("emailAddress", "zhao@example.com", MapWrapper.KeyStyle.SNAKE_CASE)
                .put("userAge", 35, MapWrapper.KeyStyle.SNAKE_CASE)
                .build();

        System.out.println("字符串 key + SNAKE_CASE:");
        System.out.println("生成的 Map: " + map4);
        map4.keySet().forEach(key -> System.out.println("  - " + key));
        System.out.println();

        // ========================================================================
        // 5. 混合使用 - Lambda 表达式的 KeyStyle vs 单个 put 的 KeyStyle
        // ========================================================================
        System.out.println("【5】混合使用 - 可以为单个 put 指定不同的 KeyStyle");
        System.out.println("-".repeat(80));

        // 创建时指定默认 NONE，但某些字段使用 SNAKE_CASE
        Map<String, Object> map5 = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.NONE)
                .put(UserProfile::getFirstName, "钱")  // 使用默认 NONE
                .put(UserProfile::getLastName, "七")   // 使用默认 NONE
                .put(false, UserProfile::getEmailAddress, "qian@example.com", MapWrapper.KeyStyle.SNAKE_CASE) // 覆盖为 SNAKE_CASE (但条件为false)
                .put(true, UserProfile::getUserAge, 40, MapWrapper.KeyStyle.SNAKE_CASE)  // 覆盖为 SNAKE_CASE
                .build();

        System.out.println("默认 NONE，部分字段指定 SNAKE_CASE:");
        System.out.println("生成的 Map: " + map5);
        map5.forEach((key, value) -> System.out.println("  - " + key + ": " + value));
        System.out.println();

        // ========================================================================
        // 6. 实际应用场景
        // ========================================================================
        System.out.println("【6】实际应用场景");
        System.out.println("-".repeat(80));
        System.out.println("✓ 对接数据库: 使用 SNAKE_CASE 符合数据库字段命名规范");
        System.out.println("✓ 对接前端: 使用 CAMEL_CASE 符合 JavaScript 命名规范");
        System.out.println("✓ 内部使用: 使用 NONE 保持 Java 原始字段名");
        System.out.println();

        // 模拟数据库参数
        Map<String, Object> dbParams = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.SNAKE_CASE)
                .put(UserProfile::getFirstName, "孙")
                .put(UserProfile::getEmailAddress, "sun@example.com")
                .put(UserProfile::getUserAge, 27)
                .build();
        System.out.println("数据库参数 (SNAKE_CASE): " + dbParams);

        // 模拟 API 响应
        Map<String, Object> apiResponse = MapWrapper.createLambda(UserProfile.class, MapWrapper.KeyStyle.CAMEL_CASE)
                .put(UserProfile::getFirstName, "周")
                .put(UserProfile::getEmailAddress, "zhou@example.com")
                .put(UserProfile::getUserAge, 32)
                .build();
        System.out.println("API 响应 (CAMEL_CASE): " + apiResponse);

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("总结:");
        System.out.println("  KeyStyle.NONE       -> 保持原样");
        System.out.println("  KeyStyle.SNAKE_CASE -> 转换为下划线命名 (first_name, email_address)");
        System.out.println("  KeyStyle.CAMEL_CASE -> 转换为驼峰命名 (firstName, emailAddress)");
        System.out.println("=".repeat(80));
    }
}
