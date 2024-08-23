package io.github.thebesteric.framework.agile.distributed.locks.generator;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
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
    public static String generate(final Method method) {
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        String distributedLockKey = distributedLock.key();
        if (distributedLockKey.isEmpty()) {
            distributedLockKey = ReflectUtils.methodSignature(method);
        }
        return distributedLock.keyPrefix() + distributedLockKey;
    }
}
