package io.github.thebesteric.framework.agile.wechat.third.platform.api.mini.message;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.commons.util.MapWrapper;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini.*;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息相关 / 订阅消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 13:32:13
 */
public class SubscribeMessageApi {

    /**
     * 删除模板
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param templateId  要删除的模板 id
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/deleteMessageTemplate.html">删除模板</a>
     * @author wangweijun
     * @since 2024/8/2 16:12
     */
    public WechatResponse deleteMessageTemplate(String accessToken, String templateId) {
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/deltemplate?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("priTmplId", templateId));
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 获取类目
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     *
     * @return CategoryResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/getCategory.html">获取类目</a>
     * @author wangweijun
     * @since 2024/8/2 16:28
     */
    public CategoryResponse getCategory(String accessToken) {
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/getcategory?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, CategoryResponse.class);
    }


    /**
     * 获取关键词列表
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param templateId  模板标题 id，可通过接口获取
     *
     * @return PubTemplateKeyWordsResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/getPubTemplateKeyWordsById.html">获取关键词列表</a>
     * @author wangweijun
     * @since 2024/8/2 16:33
     */
    public PubTemplateKeyWordsResponse getPubTemplateKeyWordsById(String accessToken, String templateId) {
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/getpubtemplatekeywords?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url, Map.of("tid", templateId));
        return BaseResponse.of(response, PubTemplateKeyWordsResponse.class);
    }

    /**
     * 获取所属类目下的公共模板
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param categoryIds 类目 ids
     * @param start       用于分页，表示从 start 开始。从 0 开始计数
     * @param limit       用于分页，表示拉取 limit 条记录。最大为 30
     *
     * @return PubTemplateTitleListResponse
     *
     * @author wangweijun
     * @since 2024/8/2 16:44
     */
    public PubTemplateTitleListResponse getPubTemplateTitleList(String accessToken, List<String> categoryIds, Integer start, Integer limit) {
        start = Math.max(0, start);
        limit = Math.min(30, limit);
        String ids = categoryIds.stream().collect(Collectors.joining(","));
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/getpubtemplatetitles?access_token=%s", accessToken);
        Map<String, Object> params = MapWrapper.create().put("ids", ids).put("start", start).put("limit", limit).build();
        HttpResponse response = HttpUtils.get(url, params);
        return BaseResponse.of(response, PubTemplateTitleListResponse.class);
    }

    /**
     * 获取个人模板列表
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     *
     * @return MessageTemplateListResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/getMessageTemplateList.html">获取个人模板列表</a>
     * @author wangweijun
     * @since 2024/8/2 16:52
     */
    public MessageTemplateListResponse getMessageTemplateList(String accessToken) {
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/gettemplate?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, MessageTemplateListResponse.class);
    }

    /**
     * 发送订阅消息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/sendMessage.html">发送订阅消息</a>
     * @author wangweijun
     * @since 2024/8/2 17:48
     */
    public WechatResponse sendMessage(String accessToken, SendMessageRequest request) {
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request.toMap());
        return BaseResponse.of(response, WechatResponse.class);
    }

    /**
     * 添加模板
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return AddMessageTemplateResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/addMessageTemplate.html">添加模板</a>
     * @author wangweijun
     * @since 2024/8/2 17:55
     */
    public AddMessageTemplateResponse addMessageTemplate(String accessToken, AddMessageTemplateRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxaapi/newtmpl/addtemplate?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, AddMessageTemplateResponse.class);
    }

    /**
     * 激活与更新服务卡片
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/setUserNotify.html">激活与更新服务卡片</a>
     * @author wangweijun
     * @since 2024/8/2 18:07
     */
    public WechatResponse setUserNotify(String accessToken, UserNotifySetRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/set_user_notify?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, AddMessageTemplateResponse.class);
    }

    /**
     * 查询服务卡片状态
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return UserNotifyGetResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/getUserNotify.html">查询服务卡片状态</a>
     * @author wangweijun
     * @since 2024/8/2 18:23
     */
    public UserNotifyGetResponse getUserNotify(String accessToken, UserNotifyGetRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/get_user_notify?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, UserNotifyGetResponse.class);
    }

    /**
     * 更新服务卡片扩展信息
     *
     * @param accessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用 access_token 或者 authorizer_access_token
     * @param request     请求
     *
     * @return WechatResponse
     *
     * @link <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/mp-message-management/subscribe-message/setUserNotifyExt.html">更新服务卡片扩展信息</a>
     * @author wangweijun
     * @since 2024/8/5 09:34
     */
    public WechatResponse setUserNotifyExt(String accessToken, UserNotifyExtRequest request) {
        String url = String.format("https://api.weixin.qq.com/wxa/set_user_notifyext?access_token=%s", accessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, WechatResponse.class);
    }

}
