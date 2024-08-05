package io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.request.ObjectParamRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 提交代码审核
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 17:01:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "提交代码审核")
public class MiniCodeSubmitAuditRequest extends ObjectParamRequest {

    @JsonProperty("item_list")
    @Schema(description = "审核项列表（选填，至多填写 5 项）；类目是必填的，且要填写已经在小程序配置好的类目")
    private List<Item> itemList = new ArrayList<>();

    @JsonProperty("feedback_info")
    @Schema(description = "反馈内容，至多 200 字", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String feedbackInfo;

    @JsonProperty("feedback_stuff")
    @Schema(description = "用 | 分割的 media_id 列表，至多 5 张图片, 可以通过新增临时素材接口上传而得到", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String feedbackStuff;

    @JsonProperty("version_desc")
    @Schema(description = "小程序版本说明和功能解释", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String versionDesc;

    @JsonProperty("preview_info")
    @Schema(description = "预览信息（小程序页面截图和操作录屏）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private PreviewInfo previewInfo;

    @JsonProperty("ugc_declare")
    @Schema(description = "用户生成内容场景（UGC）信息安全声明", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UgcDeclare ugcDeclare;

    @JsonProperty("privacy_api_not_use")
    @Schema(description = "用于声明是否不使用“代码中检测出但是未配置的隐私相关接口”", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean privacyApiNotUse;

    @JsonProperty("order_path")
    @Schema(description = "订单中心 path", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean orderPath;

    @Data
    public static class UgcDeclare {
        @Schema(description = "UGC场景 0,不涉及用户生成内容, 1.用户资料, 2.图片, 3.视频, 4.文本, 5音频, 可多选，当 scene 填 0 时无需填写下列字段")
        private List<Integer> scene = Collections.singletonList(0);

        @Schema(description = "内容安全机制 1.使用平台建议的内容安全API,2.使用其他的内容审核产品,3.通过人工审核把关,4.未做内容审核把关", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private List<Integer> method = new ArrayList<>();

        @JsonProperty("other_scene_desc")
        @Schema(description = "当 scene 选其他时的说明,不超时256字", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String otherSceneDesc;

        @JsonProperty("has_audit_team")
        @Schema(description = "是否有审核团队, 0.无, 1.有, 默认 0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private Integer hasAuditTeam = 0;

        @JsonProperty("audit_desc")
        @Schema(description = "说明当前对 UGC 内容的审核机制,不超过 256 字", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String auditDesc;
    }

    @Data
    public static class PreviewInfo {
        @JsonProperty("video_id_list")
        @Schema(description = "录屏 mediaId 列表，可以通过提审素材上传接口获得")
        private List<String> videoIdList;

        @JsonProperty("pic_id_list")
        @Schema(description = "截屏 mediaId 列表，可以通过提审素材上传接口获得")
        private List<String> picIdList;
    }


    @Data
    public static class Item {
        @Schema(description = "小程序的页面，可通过获取小程序的页面列表 getCodePage 接口获得", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String address;

        @Schema(description = "小程序的标签，用空格分隔，标签至多 10 个，标签长度至多 20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String tag;

        @Schema(description = "小程序页面的标题，标题长度至多 32", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String title;

        @JsonProperty("first_id")
        @Schema(description = "一级类目 id，可通过 getAllCategoryName 接口获取")
        private String firstId;

        @JsonProperty("second_id")
        @Schema(description = "二级类目 id，可通过 getAllCategoryName 接口获取")
        private String secondId;

        @JsonProperty("third_id")
        @Schema(description = "三级类目 id，可通过 getAllCategoryName 接口获取", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String thirdId;

        @JsonProperty("first_class")
        @Schema(description = "一级类目名称，可通过 getAllCategoryName 接口获取")
        private String firstClass;

        @JsonProperty("second_class")
        @Schema(description = "二级类目名称，可通过 getAllCategoryName 接口获取")
        private String secondClass;

        @JsonProperty("third_class")
        @Schema(description = "三级类目名称，可通过 getAllCategoryName 接口获取", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private String thirdClass;
    }
}
