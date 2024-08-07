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
 * 图片消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-07 14:35:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class CustomerImageMessageEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = 336417824350320240L;

    @JsonProperty("PicUrl")
    @XmlElement(name = "PicUrl")
    @Schema(description = "图片链接（由系统生成）")
    private String picUrl;

    @JsonProperty("MediaId")
    @XmlElement(name = "MediaId")
    @Schema(description = "图片消息媒体 ID")
    private String mediaId;

    @JsonProperty("MsgId")
    @XmlElement(name = "MsgId")
    @Schema(description = "消息 ID")
    private Long msgId;

    @XmlTransient
    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    @XmlTransient
    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    @XmlTransient
    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }
}
