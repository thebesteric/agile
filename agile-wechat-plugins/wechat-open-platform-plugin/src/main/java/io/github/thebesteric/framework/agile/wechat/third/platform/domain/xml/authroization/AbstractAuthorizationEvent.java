package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.authroization;

import io.github.thebesteric.framework.agile.wechat.third.platform.constant.third.InfoType;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.AbstractEvent;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * AbstractXml
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 12:06:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractAuthorizationEvent extends AbstractEvent {
    @Serial
    private static final long serialVersionUID = -2158367475573962135L;

    /** 第三方平台 appid */
    @XmlElement(name = "AppId")
    private String appId;

    /** 消息类型 */
    @XmlElement(name = "InfoType")
    private String infoType;

    @XmlTransient
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @XmlTransient
    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public InfoType getInfoType() {
        return InfoType.of(this.infoType);
    }
}
