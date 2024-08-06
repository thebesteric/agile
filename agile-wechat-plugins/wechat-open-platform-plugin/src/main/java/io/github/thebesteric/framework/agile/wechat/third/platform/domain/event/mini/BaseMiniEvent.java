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
 * BaseMiniEvent
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 16:51:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class BaseMiniEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = -1892717818629610829L;

    @JsonProperty("Encrypt")
    @XmlElement(name = "Encrypt")
    @Schema(description = "加密数据")
    private String encrypt;

    @XmlTransient
    public void setEncrypt(String encrypt) {
        this.encrypt = encrypt;
    }
}
