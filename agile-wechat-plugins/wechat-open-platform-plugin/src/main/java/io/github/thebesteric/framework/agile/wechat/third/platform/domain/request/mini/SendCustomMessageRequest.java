package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.MessageType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送客服消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 16:50:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "发送客服消息")
public class SendCustomMessageRequest extends ObjectParamRequest {
    @JsonProperty("touser")
    @Schema(description = "用户的 OpenID")
    private String toUser;

    @JsonProperty("msgtype")
    @Schema(description = "消息类型。text 表示文本消息；image 表示图片消息；link 表示图文链接；miniprogrampage 表示小程序卡片。")
    private MessageType msgType;

    @Schema(description = "文本消息，msgtype=\"text\" 时必填")
    private Text text;

    @Schema(description = "图片消息，msgtype=\"image\" 时必填")
    private Image image;

    @Schema(description = "图片消息，msgtype=\"link\" 时必填")
    private Link link;

    @JsonProperty("miniprogrampage")
    @Schema(description = "小程序卡片消息，msgtype=\"miniprogrampage\" 时必填")
    private MiniProgramPage miniProgramPage;

    @Data
    public static class Text {
        @Schema(description = "文本消息内容。msgtype = text 时必填")
        private String content;
    }

    @Data
    public static class Image {
        @JsonProperty("media_id")
        @Schema(description = "发送的图片的媒体ID，通过 uploadTempMedia 上传图片文件获得")
        private String content;
    }

    @Data
    public static class Link {
        @Schema(description = "消息标题")
        private String title;

        @Schema(description = "图文链接消息")
        private String description;

        @Schema(description = "图文链接消息被点击后跳转的链接")
        private String url;

        @Schema(description = "图文链接消息的图片链接，支持 JPG、PNG 格式，较好的效果为大图 640 X 320，小图 80 X 80")
        private String thumbUrl;
    }

    @Data
    public static class MiniProgramPage {
        @Schema(description = "小程序的页面标题")
        private String title;

        @JsonProperty("pagepath")
        @Schema(description = "小程序的页面路径，跟 app.json 对齐，支持参数，比如 pages/index/index?foo=bar")
        private String pagePath;

        @JsonProperty("thumb_media_id")
        @Schema(description = "小程序消息卡片的封面，image 类型的 media_id，通过 uploadTempMedia 接口上传图片文件获得，建议大小为 520*416")
        private String thumbMediaId;
    }
}
