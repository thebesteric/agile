package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 提交代码审核
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 16:59:52
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class MiniCodeSubmitAuditResponse extends WechatResponse {
    @Schema(description = "审核编号")
    @JsonProperty("auditid")
    private Integer auditId;
}
