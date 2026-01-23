package io.github.thebesteric.framework.agile.test.domain;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.AbstractUserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MyUserInfo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-23 23:53:31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class MyUserInfo extends AbstractUserInfo {

    public String username;

    @Override
    public String getIdentity() {
        return this.username;
    }
}
