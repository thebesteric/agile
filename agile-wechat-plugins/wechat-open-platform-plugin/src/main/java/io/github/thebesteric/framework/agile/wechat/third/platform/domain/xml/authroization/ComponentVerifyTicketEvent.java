package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * ComponentVerifyTicketXml
 *
 * @author wangweijun
 * @version v1.0
 * @link <a href="https://developers.weixin.qq.com/doc/oplatform/Third-party_Platforms/2.0/api/Before_Develop/component_verify_ticket.html">ComponentVerifyTicket</a>
 * @since 2024-07-29 12:58:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class ComponentVerifyTicketEvent extends AbstractAuthorizationEvent {
    @Serial
    private static final long serialVersionUID = -5272261878764296095L;

    /** 验证票据 */
    @XmlElement(name = "ComponentVerifyTicket")
    private String componentVerifyTicket;

    @XmlTransient
    public void setComponentVerifyTicket(String componentVerifyTicket) {
        this.componentVerifyTicket = componentVerifyTicket;
    }
}
