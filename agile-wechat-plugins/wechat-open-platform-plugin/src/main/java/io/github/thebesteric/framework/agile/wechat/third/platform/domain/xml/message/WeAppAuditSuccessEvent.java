package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 小程序代码审核通过通知
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 19:02:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class WeAppAuditSuccessEvent extends AbstractMessageEvent {
    @Serial
    private static final long serialVersionUID = -4345963195933593911L;

    /** 审核成功的时间 */
    @XmlElement(name = "SuccTime")
    private Long succTime;

    @XmlTransient
    public void setSuccTime(Long succTime) {
        this.succTime = succTime;
    }

    public Date getSuccTime() {
        return secondToDate(this.succTime);
    }
}
