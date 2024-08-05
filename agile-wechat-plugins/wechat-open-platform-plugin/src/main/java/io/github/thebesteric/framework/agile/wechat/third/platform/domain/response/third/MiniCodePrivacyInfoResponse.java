package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 获取隐私接口检测结果
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:46:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MiniCodePrivacyInfoResponse extends WechatResponse {
    /** 没权限的隐私接口的 api 英文名 */
    @JsonProperty("without_auth_list")
    private List<String> withoutAuthList;

    /** 没配置的隐私接口的 api 英文名 */
    @JsonProperty("without_conf_list")
    private List<String> withoutConfList;
}
