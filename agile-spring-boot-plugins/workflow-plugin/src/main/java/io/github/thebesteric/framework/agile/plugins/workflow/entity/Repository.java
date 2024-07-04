package io.github.thebesteric.framework.agile.plugins.workflow.entity;

import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;

/**
 * Repository
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-14 10:08:46
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class Repository extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -1178875905573963929L;

    /** 工作流实例 ID */
    private Integer workflowInstanceId;
    /** 附件名称 */
    private String name;
    /** 附件后缀 */
    private String suffix;
    /** 附件内容 */
    private Byte[] bytes;
}
