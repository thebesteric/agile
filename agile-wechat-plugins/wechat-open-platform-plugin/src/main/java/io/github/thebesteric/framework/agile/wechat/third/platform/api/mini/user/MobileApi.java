package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.PhoneNumberResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 用户信息 / 手机号
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:47:41
 */
public class MobileApi {

    /**
     * 获取手机号
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用access_token 或者 authorizer_access_token
     * @param code        手机号获取凭证，通过前端插件 getPhoneNumber 获取
     *
     * @return PhoneNumberResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html">获取手机号</a>
     * @author wangweijun
     * @since 2024/8/2 15:53
     */
    public PhoneNumberResponse getPhoneNumber(String accessToken, String code) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("code", code));
        return BaseResponse.of(response, PhoneNumberResponse.class);
    }

}
