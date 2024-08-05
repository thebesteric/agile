package io.github.thebesteric.framework.agile.wechat.third.platform.api.third.platform_manage.domain;

import cn.hutool.http.HttpResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.ThirdPartyJumpDomainRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third.ThirdPartyServerDomainRequest;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.ThirdPartyJumpDomainConfirmFileResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.ThirdPartyJumpDomainResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third.ThirdPartyServerDomainResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.github.thebesteric.framework.agile.wechat.third.platform.utils.HttpUtils;

import java.util.HashMap;

/**
 * 第三方平台管理-域名管理
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:36:45
 */
public class DomainManageApi {

    /**
     * 设置第三方平台服务器域名
     *
     * @param componentAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用component_access_token
     * @param request              请求
     *
     * @return ThirdPartyServerDomainResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/domain-mgnt/modifyThirdpartyServerDomain.html">设置第三方平台服务器域名</a>
     * @author wangweijun
     * @since 2024/8/1 15:02
     */
    public ThirdPartyServerDomainResponse modifyThirdPartyServerDomain(String componentAccessToken, ThirdPartyServerDomainRequest request) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/modify_wxa_server_domain?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.post(url, request);
        return BaseResponse.of(response, ThirdPartyServerDomainResponse.class);
    }

    /**
     * 设置第三方平台服务器域名
     *
     * @param componentAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用component_access_token
     * @param request              请求
     *
     * @return ThirdPartyJumpDomainResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/domain-mgnt/modifyThirdpartyJumpDomain.html">设置第三方平台服务器域名</a>
     * @author wangweijun
     * @since 2024/8/1 15:27
     */
    public ThirdPartyJumpDomainResponse modifyThirdPartyJumpDomain(String componentAccessToken, ThirdPartyJumpDomainRequest request) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/modify_wxa_server_domain?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, ThirdPartyJumpDomainResponse.class);
    }

    /**
     * 获取第三方平台业务域名校验文件
     *
     * @param componentAccessToken 接口调用凭证，该参数为 URL 参数，非 Body 参数。使用component_access_token
     *
     * @return ThirdPartyJumpDomainConfirmFileResponse
     *
     * @link <a href="https://developers.weixin.qq.com/doc/oplatform/openApi/OpenApiDoc/thirdparty-management/domain-mgnt/getThirdpartyJumpDomainConfirmFile.html">获取第三方平台业务域名校验文件</a>
     * @author wangweijun
     * @since 2024/8/1 15:13
     */
    public ThirdPartyJumpDomainConfirmFileResponse getThirdPartyJumpDomainConfirmFile(String componentAccessToken) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/get_domain_confirmfile?access_token=%s".formatted(componentAccessToken);
        HttpResponse response = HttpUtils.post(url, new HashMap<>());
        return BaseResponse.of(response, ThirdPartyJumpDomainConfirmFileResponse.class);
    }

}
