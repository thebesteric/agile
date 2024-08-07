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
 * 进入会话事件
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-07 14:35:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class CustomerSessionMessageEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = 336417824350320240L;

    @JsonProperty("SessionFrom")
    @XmlElement(name = "SessionFrom")
    @Schema(description = "开发者在客服会话按钮设置的 session-from 属性")
    private String sessionFrom;

    @XmlTransient
    public void setSessionFrom(String sessionFrom) {
        this.sessionFrom = sessionFrom;
    }
}
