package io.github.thebesteric.framework.agile.test.config;

import com.baomidou.mybatisplus.annotation.DbType;
import io.github.thebesteric.framework.agile.plugins.database.config.AbstractAgileMyBatisPlusConfig;
import io.github.thebesteric.framework.agile.test.domain.MyUserInfo;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlusConfig
 *
 * @author wangweijun
 * @version v1.0
 * @since 2026-01-23 23:37:12
 */
@Configuration
public class MyBatisPlusConfig extends AbstractAgileMyBatisPlusConfig<MyUserInfo> {

    @Override
    public MyUserInfo getCurrentUser() {
        return new MyUserInfo("test-user");
    }

    @Override
    public DbType getDbType() {
        return DbType.MYSQL;
    }

    @Override
    public boolean getPaginationOverflow() {
        return true;
    }




}
