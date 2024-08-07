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
 * 文本消息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-07 14:35:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class CustomerTextMessageEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = 336417824350320240L;

    @JsonProperty("Content")
    @XmlElement(name = "Content")
    @Schema(description = "文本消息内容")
    private String content;

    @JsonProperty("MsgId")
    @XmlElement(name = "MsgId")
    @Schema(description = "消息 ID")
    private Long msgId;

    @XmlTransient
    public void setContent(String content) {
        this.content = content;
    }

    @XmlTransient
    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }
}
