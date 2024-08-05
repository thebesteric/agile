package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini;

import lombok.Data;

/**
 * 小程序 API 基类
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 14:08:16
 */
@Data
public abstract class AbstractMiniApi {
    /** 小程序 AppId */
    protected String appId;
    /** 小程序 AppSecret */
    protected String appSecret;

    protected AbstractMiniApi(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }
}
