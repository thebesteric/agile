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
 * 模板草稿列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 09:48:04
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "模板草稿列表")
public class TemplatedRaftListResponse extends BaseResponse {

    @Schema(description = "草稿列表")
    @JsonProperty("draft_list")
    private List<DraftItem> draftList = new ArrayList<>();


    @Data
    @Schema(description = "草稿项")
    public static class DraftItem {
        @Schema(description = "草稿 id")
        @JsonProperty("draft_id")
        private Integer draftId;

        @Schema(description = "开发者上传草稿时间戳")
        @JsonProperty("create_time")
        private Integer createTime;

        @Schema(description = "版本号，开发者自定义字段")
        @JsonProperty("user_version")
        private String userVersion;

        @Schema(description = "版本描述，开发者自定义字段")
        @JsonProperty("user_desc")
        private String userDesc;
    }

}
