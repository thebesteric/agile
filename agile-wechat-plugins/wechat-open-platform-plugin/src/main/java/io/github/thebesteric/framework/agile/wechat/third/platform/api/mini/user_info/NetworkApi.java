package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.UserEncryptKeyRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.UserEncryptKeyResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

/**
 * 用户信息 / 网络
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:56:03
 */
public class NetworkApi {

    /**
     * 获取用户 encryptKey
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return UserEncryptKeyResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/internet/getUserEncryptKey.html">获取用户 encryptKey</a>
     * @author wangweijun
     * @since 2024/8/2 16:07
     */
    public UserEncryptKeyResponse getUserEncryptKey(String accessToken, UserEncryptKeyRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/getuserencryptkey?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url, request.toMap());
        return BaseResponse.of(response, UserEncryptKeyResponse.class);
    }

}
