package io.github.thebesteric.framework.agile.plugins.database.core.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 抽象的用户信息
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-23 23:14:30
 */
@Getter
@Setter
public abstract class AbstractUserInfo {

    /**
     * 获取用户唯一标识
     *
     * @return String
     *
     * @author wangweijun
     * @since 2026/1/23 23:57
     */
    public abstract String getIdentity();
}
