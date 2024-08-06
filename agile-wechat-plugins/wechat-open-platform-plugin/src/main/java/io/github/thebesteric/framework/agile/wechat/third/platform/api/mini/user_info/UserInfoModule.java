package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info;

/**
 * 用户信息
 *
 * @param userInfoApi 用户信息-用户信息
 * @param mobileApi   用户信息-手机号
 * @param networkApi  用户信息-网络
 *
 * @author wangweijun
 * @since 2024/8/6 09:49
 */
public record UserInfoModule(UserInfoApi userInfoApi, MobileApi mobileApi, NetworkApi networkApi) {
}