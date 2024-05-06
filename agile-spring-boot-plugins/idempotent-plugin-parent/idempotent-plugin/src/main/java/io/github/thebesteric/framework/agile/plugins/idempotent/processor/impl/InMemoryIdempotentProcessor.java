package io.github.thebesteric.framework.agile.plugins.idempotent.processor.impl;

import io.github.thebesteric.framework.agile.plugins.idempotent.processor.IdempotentProcessor;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * InMemoryIdempotentProcessor
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-04 17:23:21
 */
public class InMemoryIdempotentProcessor implements IdempotentProcessor {

    /** 具体存储缓存数据的容器 */
    private final Map<String, Long> dataRecords = new ConcurrentHashMap<>();
    /** 过期时间记录 */
    private final Map<String, Date> expireRecords = new ConcurrentHashMap<>();

    /**
     * 添加
     *
     * @param key      key
     * @param value    value
     * @param duration 加锁时长
     * @param timeUnit 时间单位
     *
     * @author wangweijun
     * @since 2024/4/30 17:20
     */
    public void put(String key, Long value, long duration, TimeUnit timeUnit) {
        dataRecords.put(key, value);
        expireRecords.put(key, new Date(System.currentTimeMillis() + timeUnit.toMillis(duration)));
    }

    /**
     * 删除
     *
     * @param key key
     *
     * @author wangweijun
     * @since 2024/5/4 17:28
     */
    public void remove(String key) {
        dataRecords.remove(key);
        expireRecords.remove(key);
    }

    /**
     * 尝试枷锁
     *
     * @param key      key
     * @param value    value
     * @param duration 加锁时长
     * @param timeUnit 时间单位
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2024/5/4 17:28
     */
    @Override
    public boolean tryLock(String key, Long value, long duration, TimeUnit timeUnit) {
        Date expireDate = expireRecords.get(key);
        // 没有记录过期时间的记录
        if (expireDate == null) {
            this.put(key, value, duration, timeUnit);
            return true;
        }
        // 命中缓存后 返回缓存数据
        if (new Date().before(expireDate)) {
            return false;
        }
        // 数据过期移除数据存储和过期记录存储
        this.remove(key);
        // 再次新增缓存数据
        this.put(key, value, duration, timeUnit);
        return true;
    }
}
