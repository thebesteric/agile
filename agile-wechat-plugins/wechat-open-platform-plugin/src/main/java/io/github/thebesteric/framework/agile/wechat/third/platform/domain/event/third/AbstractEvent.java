package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * AbstractEvent
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 19:14:51
 */
@Data
public abstract class AbstractEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = -7108122001433683963L;

    /** 创建时间 */
    @JsonProperty("CreateTime")
    @XmlElement(name = "CreateTime")
    private Long createTime;

    @XmlTransient
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return secondToDate(this.createTime);
    }

    public Date secondToDate(Long seconds) {
        if (seconds != null) {
            return new Date(seconds * 1000L);
        }
        return null;
    }
}
