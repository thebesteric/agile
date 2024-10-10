package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.commons.util.DateUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 审批时间段查询条件
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-10 13:27:16
 */
@Data
@Accessors(chain = true)
public class ApproveDatesSegmentCondition {

    /** 提交时间-开始时间 */
    private Date submitStartDate;
    /** 提交时间-结束时间 */
    private Date submitEndDate;
    /** 审批时间-开始时间 */
    private Date approveStartDate;
    /** 审批时间-结束时间 */
    private Date approveEndDate;

    public ApproveDatesSegmentCondition setSubmitStartDate(String submitStartDateStr) {
        this.submitStartDate = DateUtils.parseToDateTime(submitStartDateStr);
        return this;
    }

    public ApproveDatesSegmentCondition setSubmitEndDate(String submitEndDateStr) {
        this.submitEndDate = DateUtils.parseToDateTime(submitEndDateStr);
        return this;
    }

    public ApproveDatesSegmentCondition setApproveStartDate(String submitStartDateStr) {
        this.approveStartDate = DateUtils.parseToDateTime(submitStartDateStr);
        return this;
    }

    public ApproveDatesSegmentCondition setApproveEndDate(String submitEndDateStr) {
        this.approveEndDate = DateUtils.parseToDateTime(submitEndDateStr);
        return this;
    }

    public String submitStartDateToStr() {
        return DateUtils.formatToDateTime(this.submitStartDate);
    }

    public String submitEndDateToStr() {
        return DateUtils.formatToDateTime(this.submitEndDate);
    }

    public String approveStartDateToStr() {
        return DateUtils.formatToDateTime(this.approveStartDate);
    }

    public String approveEndDateToStr() {
        return DateUtils.formatToDateTime(this.approveEndDate);
    }

}
