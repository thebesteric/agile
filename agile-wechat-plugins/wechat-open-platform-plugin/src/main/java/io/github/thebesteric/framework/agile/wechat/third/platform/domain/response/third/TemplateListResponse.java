package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 10:11:09
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "模板列表")
public class TemplateListResponse extends BaseResponse {

    @Schema(description = "模板信息列表")
    @JsonProperty("template_list")
    private List<TemplateItem> templateList = new ArrayList<>();

    @Data
    @Schema(description = "模板项")
    public static class TemplateItem {
        @Schema(description = "模板 id")
        @JsonProperty("template_id")
        private Integer templateId;

        @Schema(description = "草稿 id")
        @JsonProperty("draft_id")
        private Integer draftId;

        @Schema(description = "版本描述，开发者自定义字段")
        @JsonProperty("user_desc")
        private String userDesc;

        @Schema(description = "版本号，开发者自定义字段")
        @JsonProperty("user_version")
        private String userVersion;

        @Schema(description = "开发者上传草稿时间戳")
        @JsonProperty("create_time")
        private Integer createTime;

        @Schema(description = "开发小程序的 appid")
        @JsonProperty("source_miniprogram_appid")
        private String sourceMiniProgramAppId;

        @Schema(description = "开发小程序的名称")
        @JsonProperty("source_miniprogram")
        private String sourceMiniProgram;

        @Schema(description = "0-对应普通模板，1-对应标准模板")
        @JsonProperty("template_type")
        private Integer templateType;

        @Schema(description = "标准模板的场景标签；普通模板不返回该值")
        @JsonProperty("audit_scene")
        private Integer auditScene;

        @Schema(description = "标准模板的审核状态；普通模板不返回该值")
        @JsonProperty("audit_status")
        private Integer auditStatus;

        @Schema(description = "标准模板的审核驳回原因；普通模板不返回该值")
        @JsonProperty("reason")
        private String reason;

        @Schema(description = "标准模板的类目信息；如果是普通模板则值为空的数组")
        @JsonProperty("category_list")
        private List<CategoryItem> categoryList = new ArrayList<>();
    }

    @Data
    public static class CategoryItem {
        @Schema(description = "小程序的页面，可通过获取小程序的页面列表 getCodePage 接口获得")
        private String address;

        @Schema(description = "小程序的标签，用空格分隔，标签至多 10 个，标签长度至多 20")
        private String tag;

        @Schema(description = "小程序页面的标题，标题长度至多 32")
        private String title;

        @Schema(description = "一级类目 id，可通过 getAllCategoryName 接口获取")
        @JsonProperty("first_id")
        private String firstId;

        @Schema(description = "二级类目 id，可通过 getAllCategoryName 接口获取")
        @JsonProperty("second_id")
        private String secondId;

        @Schema(description = "三级类目 id，可通过 getAllCategoryName 接口获取")
        @JsonProperty("third_id")
        private String thirdId;

        @Schema(description = "一级类目名称，可通过 getAllCategoryName 接口获取")
        @JsonProperty("first_class")
        private String firstClass;

        @Schema(description = "二级类目名称，可通过 getAllCategoryName 接口获取")
        @JsonProperty("second_class")
        private String secondClass;

        @Schema(description = "三级类目名称，可通过 getAllCategoryName 接口获取")
        @JsonProperty("third_class")
        private String thirdClass;
    }
}
