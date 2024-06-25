package io.github.thebesteric.framework.agile.commons.util;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Map 包装工具类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-09 19:37:16
 */
public class MapWrapper {

    public static class MapBuilder<T> {
        private final Class<T> clazz;
        private final Map<String, Object> params;

        public MapBuilder(Class<T> clazz, Map<String, Object> params) {
            this.clazz = clazz;
            this.params = params != null ? params : new HashMap<>();
        }

        public MapBuilder<T> put(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public MapBuilder<T> put(SFunction<T, ?> getter, Object value) {
            try {
                Method method = getter.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(getter);
                String implMethodName = serializedLambda.getImplMethodName();
                String fieldName = implMethodName.startsWith("get") ? implMethodName.substring(3) : implMethodName.substring(2);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                params.put(fieldName, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to extract field name", e);
            }
            return this;
        }

        public Map<String, Object> build() {
            return params;
        }
    }

    public static <T> MapBuilder<T> create() {
        return createLambda(null, null);
    }

    public static <T> MapBuilder<T> create(Map<String, Object> map) {
        return createLambda(null, map);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz) {
        return createLambda(clazz, null);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz, Map<String, Object> map) {
        return new MapBuilder<>(clazz, map);
    }

    @FunctionalInterface
    public interface SFunction<T, R> extends Function<T, R>, Serializable {
    }

}
