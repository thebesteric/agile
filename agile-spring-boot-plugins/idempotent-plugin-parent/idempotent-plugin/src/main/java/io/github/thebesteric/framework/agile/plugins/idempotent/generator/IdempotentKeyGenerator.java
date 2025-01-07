package io.github.thebesteric.framework.agile.plugins.idempotent.generator;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.plugins.idempotent.advisor.AgileIdempotentAdvice;
import io.github.thebesteric.framework.agile.plugins.idempotent.annotation.IdempotentKey;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * IdempotentKeyGenerator
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-30 14:58:51
 */
public class IdempotentKeyGenerator extends AbstractUtils {

    /**
     * 生成幂等 key
     *
     * @param idempotentAnnotationWrapper wrapper
     * @param method                      方法
     * @param args                        参数
     *
     * @return String key
     *
     * @author wangweijun
     * @since 2024/4/30 15:19
     */
    public static String generate(final AgileIdempotentAdvice.IdempotentAnnotationWrapper idempotentAnnotationWrapper, final Method method, final Object[] args) {
        String keyPrefix = idempotentAnnotationWrapper.getKeyPrefix();
        String delimiter = idempotentAnnotationWrapper.getDelimiter();

        StringBuilder sb = new StringBuilder();

        // 获取方法参数上的 @IdempotentKey 注解
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final IdempotentKey idempotentKey = parameters[i].getAnnotation(IdempotentKey.class);
            if (idempotentKey == null) {
                continue;
            }
            // 拼接连接符 "|" + 参数值
            sb.append(delimiter).append(args[i]);
        }

        // 获取方法参数的内的 @IdempotentKey 注解
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            final Object object = args[i];
            if (object == null) {
                continue;
            }
            // 获取注解类中所有的属性字段
            Class<?> curClass = object.getClass();
            do {
                final Field[] fields = curClass.getDeclaredFields();
                for (Field field : fields) {
                    // 判断字段上是否有 @IdempotentKey 注解
                    final IdempotentKey annotation = field.getAnnotation(IdempotentKey.class);
                    if (annotation == null) {
                        continue;
                    }
                    field.setAccessible(true);
                    // 拼接连接符 "|" + 字段值
                    sb.append(delimiter).append(ReflectionUtils.getField(field, object));
                }
                curClass = curClass.getSuperclass();
            } while (curClass != null && curClass != Object.class);
        }

        // 返回 key：默认格式为：idempotent|class#method|xxx|yyy
        return keyPrefix + delimiter + ReflectUtils.methodSignature(method) + sb;
    }
}
