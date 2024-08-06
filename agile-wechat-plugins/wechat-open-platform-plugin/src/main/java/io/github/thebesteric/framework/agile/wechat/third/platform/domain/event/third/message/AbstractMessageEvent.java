package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.message;

import io.github.thebesteric.framework.agile.wechat.third.platform.constant.third.Event;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.AbstractEvent;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * MessageEvent
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 18:50:55
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractMessageEvent extends AbstractEvent {
    @Serial
    private static final long serialVersionUID = -8187721972630178868L;

    /** 小程序的原始 ID */
    @XmlElement(name = "ToUserName")
    private String toUserName;

    /** 发送方帐号，可以是 OpenID 或系统帐号 */
    @XmlElement(name = "FromUserName")
    private String fromUserName;

    /** 消息类型 */
    @XmlElement(name = "MsgType")
    private String msgType;

    /** 事件类型 */
    @XmlElement(name = "Event")
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

    public Event getEvent() {
        return Event.of(this.event);
    }
}
