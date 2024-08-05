package io.github.thebesteric.framework.agile.wechat.third.platform.config.third;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信三方平台配置
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 11:29:51
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".wechat.third")
public class WechatThirdPlatformProperties {
    /** 是否启用 */
    private boolean enable = false;
    /** 第三方组件的 AppId */
    private String componentAppId;
    /** 第三方组件的 AppSecret */
    private String componentAppSecret;
    /** 消息校验 Token */
    private String verifyToken;
    /** 消息加解密 Key */
    private String encryptAesKey;

    public static WechatThirdPlatformProperties of(String componentAppId, String componentAppSecret, String verifyToken, String encryptAesKey) {
        WechatThirdPlatformProperties properties = new WechatThirdPlatformProperties();
        properties.componentAppId = componentAppId;
        properties.componentAppSecret = componentAppSecret;
        properties.verifyToken = verifyToken;
        properties.encryptAesKey = encryptAesKey;
        return properties;
    }
}
