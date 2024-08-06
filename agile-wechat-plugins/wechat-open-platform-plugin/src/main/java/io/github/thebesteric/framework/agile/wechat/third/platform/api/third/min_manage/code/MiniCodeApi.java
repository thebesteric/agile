package io.github.thebesteric.framework.agile.wechat.third.platform.api.third.min_manage.code;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.MiniCodeCommitRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.MiniCodeGrayReleaseRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.MiniCodeSubmitAuditRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.listener.third.MessageEventListener;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 代商家管理小程序 / 小程序代码管理
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:41:43
 */
public class MiniCodeApi {

    /**
     * 获取隐私接口检测结果
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return MiniCodePrivacyInfoResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/getCodePrivacyInfo.html">获取隐私接口检测结果</a>
     * @author wangweijun
     * @since 2024/8/1 15:48
     */
    public MiniCodePrivacyInfoResponse getCodePrivacyInfo(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/security/get_code_privacy_info?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, MiniCodePrivacyInfoResponse.class);
    }

    /**
     * 上传代码并生成体验版
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     * @param request               请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/commit.html">上传代码并生成体验版</a>
     * @author wangweijun
     * @since 2024/8/1 16:03
     */
    public WechatResponse commit(String authorizerAccessToken, MiniCodeCommitRequest request) {
        String url = "https://api.weixin.qq.com/wxa/security/get_code_privacy_info?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 获取已上传的代码页面列表
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return MiniCodePageResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/getCodePage.html">获取已上传的代码页面列表</a>
     * @author wangweijun
     * @since 2024/8/1 16:11
     */
    public MiniCodePageResponse getCodePage(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/get_page?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, MiniCodePageResponse.class);
    }

    /**
     * 获取体验版二维码
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     * @param path                  指定二维码扫码后直接进入指定页面并可同时带上参数，如：page/index?action=1
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/getTrialQRCode.html">获取体验版二维码</a>
     * @author wangweijun
     * @since 2024/8/1 16:15
     */
    public InputStream getTrialQRCode(String authorizerAccessToken, String path) {
        String url = "https://api.weixin.qq.com/wxa/get_qrcode?access_token=%s".formatted(authorizerAccessToken);
        if (path != null) {
            String encodePath = URLEncoder.encode(path, StandardCharsets.UTF_8);
            url = url.concat("&path=%s".formatted(encodePath));
        }
        return HttpUtils.getToInputStream(url);
    }

    /**
     * 提交代码审核
     * <p>当小程序有审核结果后，微信服务器会向第三方平台方的消息与事件接收 URL 以 POST 的方式推送相关通知</p>
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     * @param request               请求
     *
     * @return MiniCodeSubmitAuditResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/submitAudit.html">提交代码审核</a>
     * @author wangweijun
     * @see MessageEventListener
     * @since 2024/8/1 17:58
     */
    public MiniCodeSubmitAuditResponse submitAudit(String authorizerAccessToken, MiniCodeSubmitAuditRequest request) {
        String url = "https://api.weixin.qq.com/wxa/submit_audit?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, MiniCodeSubmitAuditResponse.class);
    }

    /**
     * 撤回代码审核
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/undoAudit.html">撤回代码审核</a>
     * @author wangweijun
     * @since 2024/8/1 19:51
     */
    public WechatResponse undoAudit(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/undocodeaudit?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 发布已通过审核的小程序
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/release.html">发布已通过审核的小程序</a>
     * @author wangweijun
     * @since 2024/8/1 19:55
     */
    public WechatResponse release(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/release?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 小程序版本回退
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return MiniCodeRevertCodeReleaseResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/revertCodeRelease.html">小程序版本回退</a>
     * @author wangweijun
     * @since 2024/8/2 09:29
     */
    public MiniCodeRevertCodeReleaseResponse revertCodeRelease(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/revertcoderelease?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, MiniCodeRevertCodeReleaseResponse.class);
    }

    /**
     * 分阶段发布（灰度发布）
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     * @param request               请求
     *
     * @return WechatResponse
     *
     * @author wangweijun
     * @since 2024/8/2 10:20
     */
    public WechatResponse grayRelease(String authorizerAccessToken, MiniCodeGrayReleaseRequest request) {
        String url = "https://api.weixin.qq.com/wxa/grayrelease?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 获取分阶段发布详情
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return MiniCodeGrayReleasePlanResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/getGrayReleasePlan.html">获取分阶段发布详情</a>
     * @author wangweijun
     * @since 2024/8/2 10:48
     */
    public MiniCodeGrayReleasePlanResponse getGrayReleasePlan(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/getgrayreleaseplan?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, MiniCodeGrayReleasePlanResponse.class);
    }

    /**
     * 取消分阶段发布
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/revertGrayRelease.html">取消分阶段发布</a>
     * @author wangweijun
     * @since 2024/8/2 10:58
     */
    public WechatResponse revertGrayRelease(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/revertgrayrelease?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 设置小程序服务状态
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     * @param pause                 是否暂停服务设置, true：可见， false：不可见
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/setVisitStatus.html">设置小程序服务状态</a>
     * @author wangweijun
     * @since 2024/8/2 10:54
     */
    public WechatResponse setVisitStatus(String authorizerAccessToken, boolean pause) {
        String url = "https://api.weixin.qq.com/wxa/change_visitstatus?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("action", pause ? "close" : "open"));
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 查询各版本用户占比
     *
     * @param authorizerAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 authorizer_access_token
     *
     * @return MiniCodeSupportVersionResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/miniprogram-management/code-management/getSupportVersion.html">查询各版本用户占比</a>
     * @author wangweijun
     * @since 2024/8/2 11:03
     */
    public MiniCodeSupportVersionResponse getSupportVersion(String authorizerAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/change_visitstatus?access_token=%s".formatted(authorizerAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, MiniCodeSupportVersionResponse.class);
    }

}
