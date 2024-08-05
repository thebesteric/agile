package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 取消授权通知
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 20:44:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class UnAuthorizedEvent extends AbstractAuthorizationEvent {
    @Serial
    private static final long serialVersionUID = -4840034624724984019L;

    /** 公众号或小程序的 appid */
    @XmlElement(name = "AuthorizerAppid")
    private String authorizerAppid;

    @XmlTransient
    public void setAuthorizerAppid(String authorizerAppid) {
        this.authorizerAppid = authorizerAppid;
    }
}
