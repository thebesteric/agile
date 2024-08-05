package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.third.Action;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设置第三方平台服务器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 14:34:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ThirdPartyServerDomainRequest extends ObjectParamRequest {
    /** 操作类型 */
    private Action action;

    /** 最多可以添加 1000 个服务器域名，以;隔开。注意：域名不需带有 http:// 等协议内容，也不能在域名末尾附加详细的 URI 地址，严格按照类似 www.qq.com 的写法 */
    @JsonProperty("wxa_server_domain")
    private String wxaServerDomain;

    /** 是否同时修改已发布版本，false-只改测试版；true-同时改测试版和全网发布版 */
    @JsonProperty("is_modify_published_together")
    private boolean modifyPublishedTogether = false;


}
