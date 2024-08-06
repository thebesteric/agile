package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.WechatResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 检查加密信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-02 15:33:12
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "检查加密信息")
public class CheckEncryptedDataResponse extends WechatResponse {

    @Schema(description = "是否是合法的数据")
    private boolean valid;

    @JsonProperty("create_time")
    @Schema(description = "加密数据生成的时间戳")
    private long createTime;

    public Date getCreateTime() {
        return secondToDate(this.createTime);
    }
}
