package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 小程序卡片消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-07 14:35:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class CustomerMiniCardMessageEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = 336417824350320240L;

    @JsonProperty("Title")
    @XmlElement(name = "Title")
    @Schema(description = "标题")
    private String title;

    @JsonProperty("AppId")
    @XmlElement(name = "AppId")
    @Schema(description = "小程序 AppId")
    private String appId;

    @JsonProperty("PagePath")
    @XmlElement(name = "PagePath")
    @Schema(description = "小程序页面路径")
    private String pagePath;

    @JsonProperty("ThumbUrl")
    @XmlElement(name = "ThumbUrl")
    @Schema(description = "封面图片的临时 CDN 链接")
    private String thumbUrl;

    @JsonProperty("ThumbMediaId")
    @XmlElement(name = "ThumbMediaId")
    @Schema(description = "封面图片的临时素材 ID")
    private String thumbMediaId;

    @JsonProperty("MsgId")
    @XmlElement(name = "MsgId")
    @Schema(description = "消息 ID")
    private Long msgId;

    @XmlTransient
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlTransient
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @XmlTransient
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    @XmlTransient
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @XmlTransient
    public void setThumbMediaId(String thumbMediaId) {
        this.thumbMediaId = thumbMediaId;
    }

    @XmlTransient
    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }
}
