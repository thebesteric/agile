package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message;

/**
 * 消息相关
 *
 * @param dynamicMessageApi   消息相关-动态消息
 * @param subscribeMessageApi 消息相关-订阅消息
 *
 * @author wangweijun
 * @since 2024/8/6 09:49
 */
public record MessageModule(DynamicMessageApi dynamicMessageApi, SubscribeMessageApi subscribeMessageApi) {
}