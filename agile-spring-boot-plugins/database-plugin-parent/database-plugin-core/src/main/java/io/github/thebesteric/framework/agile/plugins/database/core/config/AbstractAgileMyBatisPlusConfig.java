package io.github.thebesteric.framework.agile.plugins.database.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.AbstractUserInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Date;

/**
 * MyBatis Plus 配置类
 *
 * @author wangweijun
 * @since 2026/1/23 23:36
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractAgileMyBatisPlusConfig<U extends AbstractUserInfo> {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(this.getDbType());
        paginationInnerInterceptor.setOverflow(this.getPaginationOverflow());
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 乐观锁插件
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor);

        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {

            /** 自动添加创建: @TableField(fill = FieldFill.INSERT) */
            @Override
            public void insertFill(MetaObject metaObject) {
                // 获取当前用户
                AbstractUserInfo currentUser = getCurrentUser();
                // 创建时间、更新时间处理
                Date date = new Date();
                this.setFieldValByName("createdAt", date, metaObject);
                this.setFieldValByName("updatedAt", date, metaObject);
                // 创建用户、更新用户处理
                String createdBy = currentUser != null ? currentUser.getIdentity() : null;
                this.setFieldValByName("createdBy", createdBy, metaObject);
                this.setFieldValByName("updatedBy", createdBy, metaObject);
            }

            /** 自动添加修改: @TableField(fill = FieldFill.UPDATE) */
            @Override
            public void updateFill(MetaObject metaObject) {
                // 获取当前用户
                AbstractUserInfo currentUser = getCurrentUser();
                // 更新时间处理
                this.setFieldValByName("updatedAt", new Date(), metaObject);
                // 更新用户处理
                String updatedBy = currentUser != null ? currentUser.getIdentity() : null;
                this.setFieldValByName("updatedBy", updatedBy, metaObject);
            }
        };
    }

    /**
     * 获取当前用户
     *
     * @return AbstractUserInfo
     *
     * @author wangweijun
     * @since 2026/1/23 23:46
     */
    public abstract U getCurrentUser();

    /**
     * 获取数据库类型
     *
     * @return DbType
     *
     * @author wangweijun
     * @since 2026/1/23 23:46
     */
    public abstract DbType getDbType();

    /**
     * 获取分页插件溢出总页数后是否进行处理
     * <p>返回 false 表示不处理，true 表示如果超出页数的范围，会返回第一页的数据</p>
     *
     * @return boolean
     *
     * @author wangweijun
     * @since 2026/1/23 23:46
     */
    public abstract boolean getPaginationOverflow();
}