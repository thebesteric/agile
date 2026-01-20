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
 * <p>提供灵活的 Map 构建功能，支持：</p>
 * <ul>
 *   <li>链式构建 Map 对象</li>
 *   <li>支持 Lambda 表达式获取字段名</li>
 *   <li>支持条件性添加键值对</li>
 *   <li>支持多种命名风格转换 (NONE、SNAKE_CASE、CAMEL_CASE)</li>
 *   <li>支持 String key 和 Lambda getter 两种方式</li>
 * </ul>
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-09 19:37:16
 */
public class MapWrapper {

    /**
     * Map 构建器类
     * <p>
     * 用于通过链式调用逐步构建 Map 对象，支持灵活的字段添加和命名风格转换。
     *
     * @param <T> 目标类型，用于 Lambda 表达式获取字段
     * @param <V> Map 中值的类型
     */
    public static class MapBuilder<T, V> {
        /** 目标类型，用于 Lambda 表达式 */
        private final Class<T> clazz;
        /** 存储键值对的 Map 对象 */
        private final Map<String, V> params;
        /** 字段名转换风格 */
        private final KeyStyle keyStyle;

        /**
         * MapBuilder 构造方法
         *
         * @param clazz    目标类型，可为 null
         * @param keyStyle 命名风格，为 null 时默认为 NONE
         * @param params   预先存在的 Map，为 null 时创建新的 HashMap
         */
        public MapBuilder(Class<T> clazz, KeyStyle keyStyle, Map<String, V> params) {
            this.clazz = clazz;
            this.params = params != null ? params : new HashMap<>();
            this.keyStyle = keyStyle == null ? KeyStyle.NONE : keyStyle;
        }

        /**
         * 添加字符串 key 和值（无条件）
         *
         * @param key   键名
         * @param value 键值
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(String key, V value) {
            return put(true, key, value);
        }

        /**
         * 条件性地添加字符串 key 和值（使用默认 KeyStyle）
         *
         * @param condition 条件判断，为 true 时才添加
         * @param key       键名
         * @param value     键值
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(boolean condition, String key, V value) {
            return put(condition, key, value, keyStyle);
        }

        /**
         * 添加字符串 key 和值，指定特定的 KeyStyle
         *
         * @param key      键名
         * @param value    键值
         * @param keyStyle 本次操作的命名风格转换
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(String key, V value, KeyStyle keyStyle) {
            return put(true, key, value, keyStyle);
        }

        /**
         * 条件性地添加字符串 key 和值，指定特定的 KeyStyle
         *
         * @param condition 条件判断，为 true 时才添加
         * @param key       键名
         * @param value     键值
         * @param keyStyle  本次操作的命名风格转换
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(boolean condition, String key, V value, KeyStyle keyStyle) {
            if (condition) {
                params.put(convertKey(key, keyStyle), value);
            }
            return this;
        }

        /**
         * 使用 Lambda 表达式获取字段名，添加对应的值（无条件）
         *
         * @param getter Lambda 表达式，用于获取字段（如 User::getName）
         * @param value  键值
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(SFunction<T, ?> getter, V value) {
            return put(true, getter, value, keyStyle);
        }

        /**
         * 条件性地使用 Lambda 表达式获取字段名，添加对应的值（使用默认 KeyStyle）
         *
         * @param condition 条件判断，为 true 时才添加
         * @param getter    Lambda 表达式，用于获取字段（如 User::getName）
         * @param value     键值
         *
         * @return 返回 this 以支持链式调用
         */
        public MapBuilder<T, V> put(boolean condition, SFunction<T, ?> getter, V value) {
            return put(condition, getter, value, keyStyle);
        }

        /**
         * 条件性地使用 Lambda 表达式获取字段名，添加对应的值，指定特定的 KeyStyle
         * <p>
         * 通过反射获取 Lambda 表达式对应的字段名，然后按指定的 KeyStyle 转换后添加到 Map 中。
         * 支持 get/is 前缀的方法名，会自动去除前缀并转换为小写。
         *
         * @param condition 条件判断，为 true 时才添加
         * @param getter    Lambda 表达式，用于获取字段（如 User::getName）
         * @param value     键值
         * @param keyStyle  本次操作的命名风格转换
         *
         * @return 返回 this 以支持链式调用
         *
         * @throws RuntimeException 如果反射提取字段名失败
         */
        public MapBuilder<T, V> put(boolean condition, SFunction<T, ?> getter, V value, KeyStyle keyStyle) {
            if (condition) {
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
            }
            return this;
        }

        /**
         * 构建并返回最终的 Map 对象
         *
         * @return 构建好的 Map 对象
         */
        public Map<String, V> build() {
            return params;
        }
    }

    /**
     * 创建一个新的 MapBuilder 实例（不支持 Lambda 表达式）
     *
     * @param <T> 目标类型
     * @param <V> Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> create() {
        return create(null, null);
    }

    /**
     * 创建一个新的 MapBuilder 实例，指定命名风格（不支持 Lambda 表达式）
     *
     * @param keyStyle 命名风格转换方式
     * @param <T>      目标类型
     * @param <V>      Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> create(KeyStyle keyStyle) {
        return create(keyStyle, null);
    }

    /**
     * 创建一个新的 MapBuilder 实例，基于已存在的 Map（不支持 Lambda 表达式）
     *
     * @param map 初始 Map 对象，会在此基础上添加新的键值对
     * @param <T> 目标类型
     * @param <V> Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> create(Map<String, V> map) {
        return create(KeyStyle.NONE, map);
    }

    /**
     * 创建一个新的 MapBuilder 实例，指定命名风格和初始 Map（不支持 Lambda 表达式）
     *
     * @param keyStyle 命名风格转换方式
     * @param map      初始 Map 对象，会在此基础上添加新的键值对
     * @param <T>      目标类型
     * @param <V>      Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> create(KeyStyle keyStyle, Map<String, V> map) {
        return createLambda(null, keyStyle, map);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式
     *
     * @param clazz 目标类型，用于 Lambda 表达式获取字段
     * @param <T>   目标类型
     * @param <V>   Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz) {
        return createLambda(clazz, KeyStyle.NONE);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定值类型
     *
     * @param clazz      目标类型，用于 Lambda 表达式获取字段
     * @param valueClass 值的类型（主要用于类型安全，不影响功能）
     * @param <T>        目标类型
     * @param <V>        Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, Class<V> valueClass) {
        return createLambda(clazz, valueClass, KeyStyle.NONE);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定值类型和命名风格
     *
     * @param clazz      目标类型，用于 Lambda 表达式获取字段
     * @param valueClass 值的类型（主要用于类型安全，不影响功能）
     * @param keyStyle   命名风格转换方式
     * @param <T>        目标类型
     * @param <V>        Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, Class<V> valueClass, KeyStyle keyStyle) {
        return createLambda(clazz, keyStyle, null);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定初始 Map
     *
     * @param clazz      目标类型，用于 Lambda 表达式获取字段
     * @param valueClass 值的类型（主要用于类型安全，不影响功能）
     * @param map        初始 Map 对象，会在此基础上添加新的键值对
     * @param <T>        目标类型
     * @param <V>        Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, Class<V> valueClass, Map<String, V> map) {
        return createLambda(clazz, KeyStyle.NONE, map);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定命名风格和初始 Map
     *
     * @param clazz      目标类型，用于 Lambda 表达式获取字段
     * @param valueClass 值的类型（主要用于类型安全，不影响功能）
     * @param keyStyle   命名风格转换方式
     * @param map        初始 Map 对象，会在此基础上添加新的键值对
     * @param <T>        目标类型
     * @param <V>        Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, Class<V> valueClass, KeyStyle keyStyle, Map<String, V> map) {
        return createLambda(clazz, keyStyle, map);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定命名风格
     *
     * @param clazz    目标类型，用于 Lambda 表达式获取字段
     * @param keyStyle 命名风格转换方式
     * @param <T>      目标类型
     * @param <V>      Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, KeyStyle keyStyle) {
        return createLambda(clazz, keyStyle, null);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，基于已存在的 Map
     *
     * @param clazz 目标类型，用于 Lambda 表达式获取字段
     * @param map   初始 Map 对象，会在此基础上添加新的键值对
     * @param <T>   目标类型
     * @param <V>   Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例，使用默认的 NONE 命名风格
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, Map<String, V> map) {
        return createLambda(clazz, KeyStyle.NONE, map);
    }

    /**
     * 创建一个新的 MapBuilder 实例，支持 Lambda 表达式，指定命名风格和初始 Map
     * <p>
     * 这是所有 createLambda 方法的核心实现方法。
     *
     * @param clazz    目标类型，用于 Lambda 表达式获取字段，可为 null
     * @param keyStyle 命名风格转换方式，可为 null（默认为 NONE）
     * @param map      初始 Map 对象，会在此基础上添加新的键值对，可为 null
     * @param <T>      目标类型
     * @param <V>      Map 中值的类型
     *
     * @return 一个新的 MapBuilder 实例
     */
    public static <T, V> MapBuilder<T, V> createLambda(Class<T> clazz, KeyStyle keyStyle, Map<String, V> map) {
        return new MapBuilder<>(clazz, keyStyle, map);
    }

    /**
     * 可序列化的函数式接口
     * <p>
     * 继承自 {@link Function} 和 {@link Serializable}，用于支持 Lambda 表达式序列化。
     * 这使得 MapWrapper 可以通过反射获取 Lambda 表达式对应的字段名。
     *
     * @param <T> 输入参数类型
     * @param <R> 返回值类型
     */
    @FunctionalInterface
    public interface SFunction<T, R> extends Function<T, R>, Serializable {
    }

    /**
     * 字段命名风格枚举
     * <p>
     * 用于控制 Map 的键名转换规则：
     * </p>
     * <ul>
     *   <li>{@code NONE}: 不做任何转换，保持原始字段名</li>
     *   <li>{@code CAMEL_CASE}: 转换为驼峰命名法</li>
     *   <li>{@code SNAKE_CASE}: 转换为下划线蛇形命名法</li>
     * </ul>
     *
     * @see #convertKey(String, KeyStyle)
     */
    public enum KeyStyle {
        /** 不做任何转换 */
        NONE,
        /** 驼峰命名法，例如：firstName、emailAddress */
        CAMEL_CASE,
        /** 下划线蛇形命名法，例如：first_name、email_address */
        SNAKE_CASE
    }

    /**
     * 根据指定的命名风格转换字段名
     *
     * @param key      原始字段名
     * @param keyStyle 转换风格，可为 null（等同于 NONE）
     *
     * @return 转换后的字段名
     *
     * @see KeyStyle#NONE
     * @see KeyStyle#CAMEL_CASE
     * @see KeyStyle#SNAKE_CASE
     */
    public static String convertKey(String key, KeyStyle keyStyle) {
        if (keyStyle == null || KeyStyle.NONE == keyStyle) {
            return key;
        }
        return keyStyle == KeyStyle.CAMEL_CASE ? CharSequenceUtil.toCamelCase(key) : CharSequenceUtil.toUnderlineCase(key);
    }

}
