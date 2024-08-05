package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 下发客服当前输入状态
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 16:16:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "下发客服当前输入状态")
public class SetTypingRequest extends ObjectParamRequest {
    @JsonProperty("touser")
    @Schema(description = "用户的 OpenID")
    private String toUser;

    @JsonProperty("command")
    @Schema(description = "命令。Typing 表示对用户下发\"正在输入\"状态 ；CancelTyping 表示取消对用户的\"正在输入\"状态")
    private String command;
}
