package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_security;

/**
 * 小程序安全
 *
 * @param contentSecurityApi 内容安全
 * @param userRiskRankApi    安全风控
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 14:27:36
 */
public record MiniSecurityModule(ContentSecurityApi contentSecurityApi, UserRiskRankApi userRiskRankApi) {
}
