package io.github.thebesteric.framework.agile.wechat.third.platform;

import io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_credential.CredentialApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_manage.template.TemplateManageApi;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.third.WechatThirdPlatformProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.Component;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.WechatThirdPlatformListener;
import lombok.Data;

/**
 * 微信三方平台帮助类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:02:52
 */
@Data
public class WechatThirdPlatformHelper {

    private final WechatThirdPlatformProperties properties;
    private final WechatThirdPlatformListener listener;

    // 第三方平台调用凭证
    private final CredentialApi credentialApi;
    // 第三方平台管理-模版库管理
    private final TemplateManageApi templateManageApi;

    public WechatThirdPlatformHelper(final WechatThirdPlatformProperties properties) {
        this.properties = properties;
        Component component = Component.of(properties.getComponentAppId(), properties.getComponentAppSecret());
        this.listener = new WechatThirdPlatformListener(properties);
        this.credentialApi = new CredentialApi(component);
        this.templateManageApi = new TemplateManageApi();
    }

    public WechatThirdPlatformHelper(final String componentAppId, final String componentAppSecret, final String verifyToken, final String encryptAesKey) {
        this(WechatThirdPlatformProperties.of(componentAppId, componentAppSecret, verifyToken, encryptAesKey));
    }

}
