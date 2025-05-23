package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取不限制的小程序码
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 13:53:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "获取不限制的小程序码")
public class GetUnlimitedQRCodeRequest extends GetQRCodeRequest {
    @Schema(description = "默认是主页，页面 page，根路径前不要填加 /，不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面。scancode_time为系统保留参数，不允许配置")
    private String page = "pages/index/index";

    @Schema(description = "最大32个可见字符，只支持数字，大小写英文以及部分特殊字符")
    private String scene;

    @JsonProperty("check_path")
    @Schema(description = "默认是 true，检查 page 是否存在，为 false 时允许小程序未发布或者 page 不存在")
    private boolean checkPath = true;
}
