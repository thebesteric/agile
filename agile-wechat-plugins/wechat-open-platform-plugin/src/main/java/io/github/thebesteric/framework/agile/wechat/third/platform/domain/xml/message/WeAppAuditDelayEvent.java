package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 小程序代码审核延后通知
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 19:02:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class WeAppAuditDelayEvent extends AbstractMessageEvent {
    @Serial
    private static final long serialVersionUID = 1046615278899270317L;

    /** 审核延后的原因 */
    @XmlElement(name = "Reason")
    private String reason;

    /** 审核延后时的时间 */
    @XmlElement(name = "DelayTime")
    private Long delayTime;

    @XmlTransient
    public void setReason(String reason) {
        this.reason = reason;
    }

    @XmlTransient
    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public Date getDelayTime() {
        return secondToDate(this.delayTime);
    }
}
