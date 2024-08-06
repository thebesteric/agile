package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.mini_code;

/**
 * 小程序码与小程序链接
 *
 * @param miniCodeApi  小程序码与小程序链接-小程序码
 * @param shortLinkApi 小程序码与小程序链接-ShortLink
 * @param urlLinkApi   小程序码与小程序链接-UrlLink
 * @param urlSchemaApi 小程序码与小程序链接-UrlSchema
 *
 * @author wangweijun
 * @since 2024/8/6 09:49
 */
public record MiniCodeModule(MiniCodeApi miniCodeApi, ShortLinkApi shortLinkApi, UrlLinkApi urlLinkApi,
                             UrlSchemaApi urlSchemaApi) {
}
