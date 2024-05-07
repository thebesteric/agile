package io.github.thebesteric.framework.agile.plugins.limiter;

/**
 * 限流类型
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-06 17:11:56
 */
public enum RateLimitType {
    /** 默认的限流策略，针对某一个接口进行限流 */
    DEFAULT,
    /** 针对 IP 进行限流 */
    IP
}
