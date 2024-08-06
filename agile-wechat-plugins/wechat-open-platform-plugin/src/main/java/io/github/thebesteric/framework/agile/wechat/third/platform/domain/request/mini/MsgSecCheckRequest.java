package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.Scene;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文本内容安全识别
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 10:09:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "文本内容安全识别")
public class MsgSecCheckRequest extends ObjectParamRequest {

    @Schema(description = "需检测的文本内容，文本字数的上限为 2500 字，需使用 UTF-8 编码")
    private String content;

    @Schema(description = "接口版本号，2.0 版本为固定值 2")
    private Integer version = 2;

    @Schema(description = "场景枚举值（1 资料；2 评论；3 论坛；4 社交日志）")
    private Scene scene;

    @Schema(description = "用户的 openid（用户需在近两小时访问过小程序）")
    private String openid;

    @Schema(description = "文本标题，需使用 UTF-8 编码")
    private String title;

    @Schema(description = "用户昵称，需使用 UTF-8 编码")
    private String nickname;

    @Schema(description = "个性签名，该参数仅在资料类场景有效 scene=1，需使用 UTF-8 编码")
    private String signature;
}
