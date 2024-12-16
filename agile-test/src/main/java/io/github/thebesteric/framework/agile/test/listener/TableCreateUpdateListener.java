package io.github.thebesteric.framework.agile.test.listener;

import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableCreateListener;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableUpdateListener;
import org.springframework.stereotype.Component;

/**
 * TabeCreateUpdateListener
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-12-16 10:43:47
 */
@Component
public class TableCreateUpdateListener implements TableCreateListener, TableUpdateListener {
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
            return false;
        }
        return TableUpdateListener.super.preUpdateTable(tableName, jdbcTemplateHelper);
    }

    @Override
    public void postUpdateTable(String tableName, JdbcTemplateHelper jdbcTemplateHelper) {
        System.out.println("postUpdateTable ===================" + tableName + "===================");
    }
}
