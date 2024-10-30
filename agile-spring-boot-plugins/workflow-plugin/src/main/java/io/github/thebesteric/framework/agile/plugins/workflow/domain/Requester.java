package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发起人
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-10-30 14:08:36
 */
@Data
public class Requester implements Serializable {
    @Serial
    private static final long serialVersionUID = 3075812511525576814L;
    /** 发起人唯一标识 */
    private String id;
    /** 发起人名称 */
    private String name;
    /** 发起人描述 */
    private String desc;

    public static Requester of(String id) {
        return Requester.of(id, null, null);
    }

    public static Requester of(String id, String name) {
        return Requester.of(id, name, null);
    }

    public static Requester of(String id, String name, String desc) {
        Requester requester = new Requester();
        requester.id = id;
        requester.name = name;
        requester.desc = desc;
        return requester;
    }
}
