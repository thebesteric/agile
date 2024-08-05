package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上传代码并生成体验版
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 15:44:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MiniCodeCommitRequest extends ObjectParamRequest {
    /** 代码库中的代码模板 ID，可通过getTemplateList接口获取代码模板 template_id。注意，如果该模板 id 为标准模板库的模板 id，则 ext_json 可支持的参数为：{"extAppid":" ", "ext": {}, "window": {}} */
    @JsonProperty("template_id")
    private String templateId;

    /** 为了方便第三方平台的开发者引入 extAppid 的开发调试工作，引入 ext.json 配置文件概念，该参数则是用于控制 ext.json 配置文件的内容 */
    @JsonProperty("ext_json")
    private String extJson;

    /** 代码版本号，开发者可自定义，长度不要超过 64 个字符 */
    @JsonProperty("user_version")
    private String userVersion;

    /** 代码描述，开发者可自定义 */
    @JsonProperty("user_desc")
    private String userDesc;
}
