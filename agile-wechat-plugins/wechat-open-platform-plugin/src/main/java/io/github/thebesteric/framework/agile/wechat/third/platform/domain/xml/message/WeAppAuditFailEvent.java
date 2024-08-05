package io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.message;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 小程序代码审核不通过通知
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 19:02:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class WeAppAuditFailEvent extends AbstractMessageEvent {
    @Serial
    private static final long serialVersionUID = -7402429751102699899L;

    /** 审核不通过的原因 */
    @XmlElement(name = "Reason")
    private String reason;

    /** 审核不通过的截图示例。用 | 分隔的 media_id 的列表，可通过获取永久素材接口拉取截图内容 */
    @XmlElement(name = "ScreenShot")
    private String screenShot;

    /** 审核不通过的时间 */
    @XmlElement(name = "FailTime")
    private Long failTime;

    @XmlTransient
    public void setFailTime(Long failTime) {
        this.failTime = failTime;
    }

    public Date getFailTime() {
        return secondToDate(this.failTime);
    }
}
