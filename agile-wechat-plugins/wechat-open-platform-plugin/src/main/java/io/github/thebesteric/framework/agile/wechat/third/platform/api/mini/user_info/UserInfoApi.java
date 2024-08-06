package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.user_info;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.PaidUnionidRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.CheckEncryptedDataResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.PaidUnionidResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.PluginOpenPIdResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 用户信息 / 用户信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 14:24:58
 */
public class UserInfoApi {

    /**
     * 获取插件用户 openPId
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param code        通过 wx.pluginLogin 获得的插件用户标志凭证 code
     *
     * @return PluginOpenPIdResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/basic-info/getPluginOpenPId.html">获取插件用户 openPId</a>
     * @author wangweijun
     * @since 2024/8/2 14:33
     */
    public PluginOpenPIdResponse getPluginOpenPId(String accessToken, String code) {
        String url = String.format("https://api.weixin.qq.com/wxa/getpluginopenpid?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("code", code));
        return BaseResponse.of(response, PluginOpenPIdResponse.class);
    }

    /**
     * 检查加密信息
     * <p>检查加密信息是否由微信生成（当前只支持手机号加密数据），只能检测最近3天生成的加密数据</p>
     *
     * @param accessToken      接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param encryptedMsgHash 加密数据的sha256，通过Hex（Base16）编码后的字符串
     *
     * @return CheckEncryptedDataResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/basic-info/checkEncryptedData.html">检查加密信息</a>
     * @author wangweijun
     * @since 2024/8/2 15:37
     */
    public CheckEncryptedDataResponse checkEncryptedData(String accessToken, String encryptedMsgHash) {
        String url = String.format("https://api.weixin.qq.com/wxa/business/checkencryptedmsg?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("encrypted_msg_hash", encryptedMsgHash));
        return BaseResponse.of(response, CheckEncryptedDataResponse.class);
    }

    /**
     * 支付后获取 UnionID
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return PaidUnionidResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/basic-info/getPaidUnionid.html">支付后获取 UnionID</a>
     * @author wangweijun
     * @since 2024/8/2 15:45
     */
    public PaidUnionidResponse getPaidUnionId(String accessToken, PaidUnionidRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/getpaidunionid?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url, request.toMap());
        return BaseResponse.of(response, PaidUnionidResponse.class);
    }


}
