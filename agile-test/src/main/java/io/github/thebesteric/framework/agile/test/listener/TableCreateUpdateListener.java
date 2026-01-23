package io.github.thebesteric.framework.agile.test.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableCreateListener;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableDropListener;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableUpdateListener;
import org.springframework.stereotype.Component;

/**
 * 表创建更新监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-16 10:43:47
 */
@Component
public class TableCreateUpdateListener implements TableCreateListener, TableUpdateListener, TableDropListener {
    @Override
    public boolean preCreateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("preCreateTable ===================" + tableName + "===================");
        return TableCreateListener.super.preCreateTable(tableName, jdbcTemplateHelper);
    }

    @Override
    public void postCreateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("postCreateTable ===================" + tableName + "===================");
    }

    @Override
    public boolean preUpdateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("preUpdateTable ===================" + tableName + "===================");
        if ("foo".equals(tableName)) {
            System.out.println("准备更新表 foo，但被拒绝");
            return false;
        }
        return TableUpdateListener.super.preUpdateTable(tableName, jdbcTemplateHelper);
    }

    @Override
    public void postUpdateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("postUpdateTable ===================" + tableName + "===================");
    }

    @Override
    public boolean preDropTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("preDropTable ===================" + tableName + "===================");
        return true;
    }

    @Override
    public void postDropTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("postDropTable ===================" + tableName + "===================");
    }
}
