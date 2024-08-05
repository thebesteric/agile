package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 将草稿添加到模板库
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-31 18:39:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AddToTemplateRequest extends ObjectParamRequest {
    /** 草稿 ID */
    @JsonProperty("draft_id")
    private Integer draftId;

    /** 模板类型：默认值是0，对应普通模板；可选1，对应标准模板库 */
    @JsonProperty("template_type")
    private Integer templateType = 0;
}
