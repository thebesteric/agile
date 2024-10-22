package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import io.github.thebesteric.framework.agile.commons.util.DateUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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

    public ApproveDatesSegmentCondition setSubmitStartDate(Date submitStartDate) {
        this.submitStartDate = submitStartDate;
        return this;
    }

    public ApproveDatesSegmentCondition setSubmitStartDate(String submitStartDateStr) {
        if (StringUtils.isNotEmpty(submitStartDateStr)) {
            this.submitStartDate = DateUtils.parseToDateTime(submitStartDateStr);
        }
        return this;
    }

    public ApproveDatesSegmentCondition setSubmitEndDate(Date submitEndDate) {
        this.submitStartDate = submitEndDate;
        return this;
    }

    public ApproveDatesSegmentCondition setSubmitEndDate(String submitEndDateStr) {
        if (StringUtils.isNotEmpty(submitEndDateStr)) {
            this.submitEndDate = DateUtils.parseToDateTime(submitEndDateStr);
        }
        return this;
    }

    public ApproveDatesSegmentCondition setApproveStartDate(Date approveStartDate) {
        this.approveStartDate = approveStartDate;
        return this;
    }

    public ApproveDatesSegmentCondition setApproveStartDate(String approveStartDateStr) {
        if (StringUtils.isNotEmpty(approveStartDateStr)) {
            this.approveStartDate = DateUtils.parseToDateTime(approveStartDateStr);
        }
        return this;
    }

    public ApproveDatesSegmentCondition setApproveEndDate(Date approveEndDate) {
        this.approveEndDate = approveEndDate;
        return this;
    }

    public ApproveDatesSegmentCondition setApproveEndDate(String approveEndDateStr) {
        if (StringUtils.isNotEmpty(approveEndDateStr)) {
            this.approveEndDate = DateUtils.parseToDateTime(approveEndDateStr);
        }
        return this;
    }

    public String submitStartDateToStr() {
        if (this.submitStartDate == null) {
            return null;
        }
        return DateUtils.formatToDateTime(this.submitStartDate);
    }

    public String submitEndDateToStr() {
        if (this.submitEndDate == null) {
            return null;
        }
        return DateUtils.formatToDateTime(this.submitEndDate);
    }

    public String approveStartDateToStr() {
        if (this.approveStartDate == null) {
            return null;
        }
        return DateUtils.formatToDateTime(this.approveStartDate);
    }

    public String approveEndDateToStr() {
        if (this.approveEndDate == null) {
            return null;
        }
        return DateUtils.formatToDateTime(this.approveEndDate);
    }

}
