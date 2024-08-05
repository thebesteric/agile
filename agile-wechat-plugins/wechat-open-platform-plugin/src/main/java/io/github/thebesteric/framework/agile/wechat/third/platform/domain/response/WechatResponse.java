package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-26 17:10:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WechatResponse extends BaseResponse {

    /** 错误码 */
    private Integer errcode;
    /** 错误信息 */
    private String errmsg;

    public boolean isSuccess() {
        return errcode == 0;
    }

    public static WechatResponse of(Integer errcode, String errmsg) {
        WechatResponse wechatResponse = new WechatResponse();
        wechatResponse.errcode = errcode;
        wechatResponse.errmsg = errmsg;
        return wechatResponse;
    }
}
