package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.ApproveStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 自定义 ApproveStatus 返回的消息格式
 *
 * @author wangweijun
 * @since 2025/1/6 14:43
 */
@Slf4j
@Getter
public class ApproveStatusDescCustomizer extends AbstractCodeDescCustomizer {

    private Pair<Integer, String> suspend = Pair.of(ApproveStatus.SUSPEND.getCode(), ApproveStatus.SUSPEND.getDesc());
    private Pair<Integer, String> inProgress = Pair.of(ApproveStatus.IN_PROGRESS.getCode(), ApproveStatus.IN_PROGRESS.getDesc());
    private Pair<Integer, String> approved = Pair.of(ApproveStatus.APPROVED.getCode(), ApproveStatus.APPROVED.getDesc());
    private Pair<Integer, String> rejected = Pair.of(ApproveStatus.REJECTED.getCode(), ApproveStatus.REJECTED.getDesc());
    private Pair<Integer, String> abandoned = Pair.of(ApproveStatus.ABANDONED.getCode(), ApproveStatus.ABANDONED.getDesc());
    private Pair<Integer, String> skipped = Pair.of(ApproveStatus.SKIPPED.getCode(), ApproveStatus.SKIPPED.getDesc());
    private Pair<Integer, String> reassigned = Pair.of(ApproveStatus.REASSIGNED.getCode(), ApproveStatus.REASSIGNED.getDesc());
    private Pair<Integer, String> interrupted = Pair.of(ApproveStatus.INTERRUPTED.getCode(), ApproveStatus.INTERRUPTED.getDesc());

    private ApproveStatusDescCustomizer() {
    }

    public static ApproveStatusDescCustomizer.ApproveStatusCustomizerBuilder builder() {
        return new ApproveStatusDescCustomizer.ApproveStatusCustomizerBuilder(new ApproveStatusDescCustomizer());
    }

    public static class ApproveStatusCustomizerBuilder {

        private final ApproveStatusDescCustomizer approveStatusDescCustomizer;

        public ApproveStatusCustomizerBuilder(ApproveStatusDescCustomizer approveStatusCustomizer) {
            this.approveStatusDescCustomizer = approveStatusCustomizer;
        }

        public ApproveStatusCustomizerBuilder suspend(String desc) {
            this.approveStatusDescCustomizer.suspend = Pair.of(ApproveStatus.SUSPEND.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder inProgress(String desc) {
            this.approveStatusDescCustomizer.inProgress = Pair.of(ApproveStatus.IN_PROGRESS.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder approved(String desc) {
            this.approveStatusDescCustomizer.approved = Pair.of(ApproveStatus.APPROVED.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder rejected(String desc) {
            this.approveStatusDescCustomizer.rejected = Pair.of(ApproveStatus.REJECTED.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder abandoned(String desc) {
            this.approveStatusDescCustomizer.abandoned = Pair.of(ApproveStatus.ABANDONED.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder skipped(String desc) {
            this.approveStatusDescCustomizer.skipped = Pair.of(ApproveStatus.SKIPPED.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder reassigned(String desc) {
            this.approveStatusDescCustomizer.reassigned = Pair.of(ApproveStatus.REASSIGNED.getCode(), desc);
            return this;
        }

        public ApproveStatusCustomizerBuilder interrupted(String desc) {
            this.approveStatusDescCustomizer.interrupted = Pair.of(ApproveStatus.INTERRUPTED.getCode(), desc);
            return this;
        }

        public ApproveStatusDescCustomizer build() {
            return this.approveStatusDescCustomizer;
        }
    }

    public Map<String, String> toMap(@Nonnull ApproveStatus approveStatus) {
        return this.toMap(approveStatus.getCode(), approveStatus.getDesc());
    }
}