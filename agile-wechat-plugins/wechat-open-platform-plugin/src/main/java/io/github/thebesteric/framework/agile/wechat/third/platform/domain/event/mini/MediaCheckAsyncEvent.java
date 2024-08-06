package io.github.thebesteric.framework.agile.wechat.third.platform.domain.event.mini;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.thebesteric.framework.agile.wechat.third.platform.constant.mini.ContentLabel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 音视频内容安全识别
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-06 13:50:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
@XmlRootElement(name = "xml")
public class MediaCheckAsyncEvent extends AbstractMiniEvent {
    @Serial
    private static final long serialVersionUID = 4082824887446336875L;

    @JsonProperty("errcode")
    @XmlElement(name = "errcode")
    @Schema(description = "响应码")
    private String errCode;

    @JsonProperty("errmsg")
    @XmlElement(name = "errmsg")
    @Schema(description = "响应消息")
    private String errMsg;

    @JsonProperty("appid")
    @XmlElement(name = "appid")
    @Schema(description = "小程序 AppId")
    private String appId;

    @JsonProperty("trace_id")
    @XmlElement(name = "trace_id")
    @Schema(description = "任务 Id")
    private String traceId;

    @JsonProperty("version")
    @XmlElement(name = "version")
    @Schema(description = "可用于区分接口版本")
    private Integer version;

    @JsonProperty("detail")
    @XmlElement(name = "detail")
    @Schema(description = "详细检测结果")
    private List<Detail> detail;

    @JsonProperty("result")
    @XmlElement(name = "result")
    @Schema(description = "综合结果")
    private Result result;


    @XmlTransient
    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    @XmlTransient
    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @XmlTransient
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @XmlTransient
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @XmlTransient
    public void setVersion(Integer version) {
        this.version = version;
    }

    @XmlTransient
    public void setDetail(List<Detail> detail) {
        this.detail = detail;
    }

    @XmlTransient
    public void setResult(Result result) {
        this.result = result;
    }

    @Data
    public static class Detail {

        @XmlElement(name = "strategy")
        @Schema(description = "策略类型")
        private String strategy;

        @XmlElement(name = "errcode")
        @JsonProperty("errcode")
        @Schema(description = "错误码，仅当该值为 0 时，该项结果有效")
        private Integer errCode;

        @XmlElement(name = "suggest")
        @Schema(description = "建议，有 risky、pass、review 三种值")
        private String suggest;

        @XmlElement(name = "label")
        @Schema(description = "命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他")
        private ContentLabel label;

        @XmlElement(name = "keyword")
        @Schema(description = "命中的自定义关键词")
        private String keyword;

        @XmlElement(name = "prob")
        @Schema(description = "0-100，代表置信度，越高代表越有可能属于当前返回的标签（label）")
        private Integer prob;

        @XmlTransient
        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        @XmlTransient
        public void setErrCode(Integer errCode) {
            this.errCode = errCode;
        }

        @XmlTransient
        public void setSuggest(String suggest) {
            this.suggest = suggest;
        }

        @XmlTransient
        public void setLabel(ContentLabel label) {
            this.label = label;
        }

        @XmlTransient
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        @XmlTransient
        public void setProb(Integer prob) {
            this.prob = prob;
        }
    }

    @Data
    public static class Result {

        @XmlElement(name = "suggest")
        @Schema(description = "建议，有 risky、pass、review 三种值")
        private String suggest;

        @XmlElement(name = "label")
        @Schema(description = "命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他")
        private ContentLabel label;

        @XmlTransient
        public void setSuggest(String suggest) {
            this.suggest = suggest;
        }

        @XmlTransient
        public void setLabel(ContentLabel label) {
            this.label = label;
        }
    }
}
