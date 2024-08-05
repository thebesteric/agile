package io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_manage.template;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.AddToTemplateRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.TemplateListResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.TemplatedRaftListResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.Map;

/**
 * 第三方平台管理-模版库管理
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:36:16
 */
public class TemplateManageApi {

    /**
     * 将草稿添加到模板库
     *
     * @param componentAccessToken 接口调用凭证，使用 component_access_token
     * @param request              请求
     *
     * @return BaseResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/template-management/addToTemplate.html">将草稿添加到模板库</a>
     * @author wangweijun
     * @since 2024/7/31 18:45
     */
    public BaseResponse addToTemplate(String componentAccessToken, AddToTemplateRequest request) {
        String url = "https://api.weixin.qq.com/wxa/addtotemplate?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response);
    }

    /**
     * 获取草稿箱列表
     *
     * @param componentAccessToken 接口调用凭证，使用 component_access_token
     *
     * @return TemplatedRaftListResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/template-management/getTemplatedRaftList.html">获取草稿箱列表</a>
     * @author wangweijun
     * @since 2024/8/1 10:06
     */
    public TemplatedRaftListResponse getTemplateDraftList(String componentAccessToken) {
        String url = "https://api.weixin.qq.com/wxa/gettemplatedraftlist?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, TemplatedRaftListResponse.class);
    }

    /**
     * 获取模板列表
     *
     * @param componentAccessToken 接口调用凭证，使用 component_access_token
     * @param templateType         可选是0（对应普通模板）和1（对应标准模板），如果不填，则返回全部
     *
     * @return TemplateListResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/template-management/getTemplateList.html">获取模板列表</a>
     * @author wangweijun
     * @since 2024/8/1 10:37
     */
    public TemplateListResponse getTemplateList(String componentAccessToken, Integer templateType) {
        String url = "https://api.weixin.qq.com/wxa/gettemplatelist?access_token=%s".formatted(componentAccessToken);
        if (templateType != null) {
            url += "&template_type=%s".formatted(templateType);
        }
        HttpResponse response = HttpUtils.get(url);
        return BaseResponse.of(response, TemplateListResponse.class);
    }

    /**
     * 删除代码模板
     *
     * @param componentAccessToken 接口调用凭证，使用 component_access_token
     * @param templateId           要删除的模板 ID，可通过获取模板列表接口
     *
     * @return BaseResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/template-management/deleteTemplate.html">删除代码模板</a>
     * @author wangweijun
     * @since 2024/8/1 10:43
     */
    public BaseResponse deleteTemplate(String componentAccessToken, Integer templateId) {
        String url = "https://api.weixin.qq.com/wxa/deletetemplate?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.post(url, Map.of("template_id", templateId));
        return BaseResponse.of(response);
    }


}
