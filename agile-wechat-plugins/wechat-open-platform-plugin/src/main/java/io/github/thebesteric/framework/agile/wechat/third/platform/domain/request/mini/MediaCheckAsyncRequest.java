package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MediaCheckType;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.Scene;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 音视频内容安全识别
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:44:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "音视频内容安全识别")
public class MediaCheckAsyncRequest extends ObjectParamRequest {

    @JsonProperty("media_url")
    @Schema(description = "要检测的图片或音频的 url，支持图片格式包括 jpg, jpeg, png, bmp, gif（取首帧），支持的音频格式包括 mp3, aac, ac3, wma, flac, vorbis, opus, wav")
    private String mediaUrl;

    @JsonProperty("media_type")
    @Schema(description = "媒体类型枚举值（1-音频; 2-图片）")
    private MediaCheckType mediaType;

    @Schema(description = "接口版本号，2.0 版本为固定值 2")
    private Integer version = 2;

    @Schema(description = "场景枚举值（1 资料；2 评论；3 论坛；4 社交日志）")
    private Scene scene;

    @JsonProperty("openid")
    @Schema(description = "用户的 openid（用户需在近两小时访问过小程序）")
    private String openId;


}
