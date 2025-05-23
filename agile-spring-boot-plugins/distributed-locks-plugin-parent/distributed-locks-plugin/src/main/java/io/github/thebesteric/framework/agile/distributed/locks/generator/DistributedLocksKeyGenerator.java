package io.github.thebesteric.framework.agile.distributed.locks.generator;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.commons.util.ConditionMatcher;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.distributed.locks.annotation.DistributedLock;

import java.lang.reflect.Method;

/**
 * DistributedLocksKeyGenerator
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 17:36:32
 */
public class DistributedLocksKeyGenerator extends AbstractUtils {

    /**
     * 生成 key
     *
     * @param method 调用方法
     *
     * @return String
     *
     * @author wangweijun
     * @since 2024/8/22 17:40
     */
    public static String generate(final Method method, final Object[] arguments) {
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        String distributedLockKey = distributedLock.key();
        // 使用方法全限定名作为 key
        if (distributedLockKey.isEmpty()) {
            distributedLockKey = ReflectUtils.methodSignature(method);
        }
        // 使用参数表达式作为 key
        else {
            distributedLockKey = ConditionMatcher.parseExpression(distributedLockKey, method.getParameters(), arguments);
        }
        return distributedLock.keyPrefix() + distributedLockKey;
    }
}
