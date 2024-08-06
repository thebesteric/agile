package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.AbstractEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AbstractMiniEvent
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 13:41:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractMiniEvent extends AbstractEvent {
    @Serial
    private static final long serialVersionUID = -8521526551110453402L;

    @JsonProperty("ToUserName")
    @XmlElement(name = "ToUserName")
    @Schema(description = "接受者 OpenId")
    private String toUserName;

    @JsonProperty("FromUserName")
    @XmlElement(name = "FromUserName")
    @Schema(description = "发送者 OpenId")
    private String fromUserName;

    @JsonProperty("MsgType")
    @XmlElement(name = "MsgType")
    @Schema(description = "消息类型，event 或 text")
    private String msgType;

    @JsonProperty("Event")
    @XmlElement(name = "Event")
    @Schema(description = "事件类型")
    private String event;

    @XmlTransient
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    @XmlTransient
    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    @XmlTransient
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    @XmlTransient
    public void setEvent(String event) {
        this.event = event;
    }
}
