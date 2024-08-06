package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MediaType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 新增图片素材
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 16:37:47
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "新增图片素材")
public class UploadTempMediaResponse extends WechatResponse {
    @Schema(description = "媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb，主要用于视频与音乐格式的缩略图）")
    private MediaType type;

    @JsonProperty("media_id")
    @Schema(description = "媒体文件上传后，获取标识，3天内有效")
    private String mediaId;

    @JsonProperty("created_at")
    @Schema(description = "媒体文件上传时间戳")
    private Long createdAt;
}
