package io.github.thebesteric.framework.agile.commons.util;

import cn.hutool.core.text.CharSequenceUtil;

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
        private final KeyStyle keyStyle;

        public MapBuilder(Class<T> clazz, KeyStyle keyStyle, Map<String, Object> params) {
            this.clazz = clazz;
            this.params = params != null ? params : new HashMap<>();
            this.keyStyle = keyStyle == null ? KeyStyle.NONE : keyStyle;
        }

        public MapBuilder<T> put(String key, Object value) {
            return put(key, value, keyStyle);
        }

        public MapBuilder<T> put(String key, Object value, KeyStyle keyStyle) {
            params.put(convertKey(key, keyStyle), value);
            return this;
        }

        public MapBuilder<T> put(SFunction<T, ?> getter, Object value) {
            return put(getter, value, keyStyle);
        }

        public MapBuilder<T> put(SFunction<T, ?> getter, Object value, KeyStyle keyStyle) {
            try {
                Method method = getter.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(getter);
                String implMethodName = serializedLambda.getImplMethodName();
                String fieldName = implMethodName.startsWith("get") ? implMethodName.substring(3) : implMethodName.substring(2);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                params.put(convertKey(fieldName, keyStyle), value);
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
        return create(null, null);
    }

    public static <T> MapBuilder<T> create(KeyStyle keyStyle) {
        return create(keyStyle, null);
    }

    public static <T> MapBuilder<T> create(Map<String, Object> map) {
        return create(KeyStyle.NONE, map);
    }


    public static <T> MapBuilder<T> create(KeyStyle keyStyle, Map<String, Object> map) {
        return createLambda(null, keyStyle, map);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz) {
        return createLambda(clazz, KeyStyle.NONE);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz, KeyStyle keyStyle) {
        return createLambda(clazz, keyStyle, null);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz, Map<String, Object> map) {
        return createLambda(clazz, KeyStyle.NONE, map);
    }

    public static <T> MapBuilder<T> createLambda(Class<T> clazz, KeyStyle keyStyle, Map<String, Object> map) {
        return new MapBuilder<>(clazz, keyStyle, map);
    }

    @FunctionalInterface
    public interface SFunction<T, R> extends Function<T, R>, Serializable {
    }

    public enum KeyStyle {
        NONE, CAMEL_CASE, SNAKE_CASE
    }

    public static String convertKey(String key, KeyStyle keyStyle) {
        if (keyStyle == null || KeyStyle.NONE == keyStyle) {
            return key;
        }
        return keyStyle == KeyStyle.CAMEL_CASE ? CharSequenceUtil.toCamelCase(key) : CharSequenceUtil.toUnderlineCase(key);
    }

}
