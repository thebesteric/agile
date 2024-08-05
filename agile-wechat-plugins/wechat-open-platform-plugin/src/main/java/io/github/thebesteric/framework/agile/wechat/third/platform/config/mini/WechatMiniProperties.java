package io.github.thebesteric.framework.agile.wechat.third.platform.config.mini;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序配置
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 13:58:08
 */
@Data
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX + ".wechat.mini")
public class WechatMiniProperties {
    /** 是否启用 */
    private boolean enable = false;
    /** 小程序唯一凭证，即 AppID，可在「微信公众平台 - 设置 - 开发设置」页中获得。（需要已经成为开发者，且帐号没有异常状态） */
    private String appId;
    /** 小程序唯一凭证密钥，即 AppSecret，获取方式同 appid */
    private String appSecret;
}
