package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.core.domain.Pair;
import io.github.thebesteric.framework.agile.plugins.workflow.constant.RoleApproveStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * 自定义 RoleApproveStatus 返回的消息格式
 *
 * @author wangweijun
 * @since 2025/1/6 14:43
 */
@Slf4j
@Getter
public class RoleApproveStatusDescCustomizer extends AbstractCodeDescCustomizer {

    private Pair<Integer, String> suspend = Pair.of(RoleApproveStatus.SUSPEND.getCode(), RoleApproveStatus.SUSPEND.getDesc());
    private Pair<Integer, String> inProgress = Pair.of(RoleApproveStatus.IN_PROGRESS.getCode(), RoleApproveStatus.IN_PROGRESS.getDesc());
    private Pair<Integer, String> approved = Pair.of(RoleApproveStatus.APPROVED.getCode(), RoleApproveStatus.APPROVED.getDesc());
    private Pair<Integer, String> rejected = Pair.of(RoleApproveStatus.REJECTED.getCode(), RoleApproveStatus.REJECTED.getDesc());
    private Pair<Integer, String> abandoned = Pair.of(RoleApproveStatus.ABANDONED.getCode(), RoleApproveStatus.ABANDONED.getDesc());
    private Pair<Integer, String> skipped = Pair.of(RoleApproveStatus.SKIPPED.getCode(), RoleApproveStatus.SKIPPED.getDesc());
    private Pair<Integer, String> reassigned = Pair.of(RoleApproveStatus.REASSIGNED.getCode(), RoleApproveStatus.REASSIGNED.getDesc());
    private Pair<Integer, String> interrupted = Pair.of(RoleApproveStatus.INTERRUPTED.getCode(), RoleApproveStatus.INTERRUPTED.getDesc());

    private RoleApproveStatusDescCustomizer() {
    }

    public static RoleApproveStatusDescCustomizer.RoleApproveStatusCustomizerBuilder builder() {
        return new RoleApproveStatusDescCustomizer.RoleApproveStatusCustomizerBuilder(new RoleApproveStatusDescCustomizer());
    }

    public static class RoleApproveStatusCustomizerBuilder {

        private final RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer;

        public RoleApproveStatusCustomizerBuilder(RoleApproveStatusDescCustomizer roleApproveStatusDescCustomizer) {
            this.roleApproveStatusDescCustomizer = roleApproveStatusDescCustomizer;
        }

        public RoleApproveStatusCustomizerBuilder suspend(String desc) {
            this.roleApproveStatusDescCustomizer.suspend = Pair.of(RoleApproveStatus.SUSPEND.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder inProgress(String desc) {
            this.roleApproveStatusDescCustomizer.inProgress = Pair.of(RoleApproveStatus.IN_PROGRESS.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder approved(String desc) {
            this.roleApproveStatusDescCustomizer.approved = Pair.of(RoleApproveStatus.APPROVED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder rejected(String desc) {
            this.roleApproveStatusDescCustomizer.rejected = Pair.of(RoleApproveStatus.REJECTED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder abandoned(String desc) {
            this.roleApproveStatusDescCustomizer.abandoned = Pair.of(RoleApproveStatus.ABANDONED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder skipped(String desc) {
            this.roleApproveStatusDescCustomizer.skipped = Pair.of(RoleApproveStatus.SKIPPED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder reassigned(String desc) {
            this.roleApproveStatusDescCustomizer.reassigned = Pair.of(RoleApproveStatus.REASSIGNED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusCustomizerBuilder interrupted(String desc) {
            this.roleApproveStatusDescCustomizer.interrupted = Pair.of(RoleApproveStatus.INTERRUPTED.getCode(), desc);
            return this;
        }

        public RoleApproveStatusDescCustomizer build() {
            return this.roleApproveStatusDescCustomizer;
        }
    }

    public Map<String, String> toMap(@Nonnull RoleApproveStatus roleApproveStatus) {
        return this.toMap(roleApproveStatus.getCode(), roleApproveStatus.getDesc());
    }
}