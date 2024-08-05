package io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.third;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板草稿列表
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-01 09:48:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TemplatedRaftListResponse extends BaseResponse {

    /** 草稿列表 */
    @JsonProperty("draft_list")
    private List<DraftItem> draftList = new ArrayList<>();

    /**
     * 草稿项
     */
    @Data
    public static class DraftItem {
        /** 草稿id */
        @JsonProperty("draft_id")
        private Integer draftId;

        /** 开发者上传草稿时间戳 */
        @JsonProperty("create_time")
        private Integer createTime;

        /** 版本号，开发者自定义字段 */
        @JsonProperty("user_version")
        private String userVersion;

        /** 版本描述，开发者自定义字段 */
        @JsonProperty("user_desc")
        private String userDesc;
    }

}
