package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 获取第三方平台业务域名校验文件
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:10:40
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取第三方平台业务域名校验文件")
public class ThirdPartyJumpDomainConfirmFileResponse extends WechatResponse {
    @Schema(description = "文件名")
    @JsonProperty("file_name")
    private String fileName;

    @Schema(description = "文件内容")
    @JsonProperty("file_content")
    private String fileContent;
}
