package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third.authroization;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AuthorizationEventXml
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 12:03:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class AuthorizationEvent extends AbstractAuthorizationEvent {
    @Serial
    private static final long serialVersionUID = 770281788293969481L;

    /** 加密字符串 */
    @XmlElement(name = "Encrypt")
    private String encrypt;

    @XmlTransient
    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }

}
