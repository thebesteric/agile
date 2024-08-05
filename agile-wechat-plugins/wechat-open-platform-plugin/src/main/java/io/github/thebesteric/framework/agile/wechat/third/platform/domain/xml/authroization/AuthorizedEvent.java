package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 授权成功通知
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 20:19:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class AuthorizedEvent extends AbstractAuthorizationEvent {
    @Serial
    private static final long serialVersionUID = 5829413426750932860L;

    /** 公众号或小程序的 appid */
    @XmlElement(name = "AuthorizerAppid")
    private String authorizerAppid;

    /** 授权码 */
    @XmlElement(name = "AuthorizationCode")
    private String authorizationCode;

    /** 授权码过期时间：单位秒 */
    @XmlElement(name = "AuthorizationCodeExpiredTime")
    private String authorizationCodeExpiredTime;

    /** 预授权码 */
    @XmlElement(name = "PreAuthCode")
    private String preAuthCode;

    @XmlTransient
    public void setAuthorizerAppid(String authorizerAppid) {
        this.authorizerAppid = authorizerAppid;
    }

    @XmlTransient
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    @XmlTransient
    public void setAuthorizationCodeExpiredTime(String authorizationCodeExpiredTime) {
        this.authorizationCodeExpiredTime = authorizationCodeExpiredTime;
    }

    @XmlTransient
    public void setPreAuthCode(String preAuthCode) {
        this.preAuthCode = preAuthCode;
    }
}
