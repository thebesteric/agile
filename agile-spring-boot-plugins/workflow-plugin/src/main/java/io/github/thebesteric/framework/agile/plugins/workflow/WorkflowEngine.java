package io.github.thebesteric.framework.agile.plugins.workflow;

import io.github.thebesteric.framework.agile.plugins.database.core.domain.ColumnDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.EntityClassDomain;
import io.github.thebesteric.framework.agile.plugins.database.core.domain.TableColumn;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.JdbcTemplateHelper;
import io.github.thebesteric.framework.agile.plugins.database.core.jdbc.TableMetadataHelper;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowProperties;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.*;
import io.github.thebesteric.framework.agile.plugins.workflow.entity.base.BaseEntity;
import io.github.thebesteric.framework.agile.plugins.workflow.exception.WorkflowException;
import io.github.thebesteric.framework.agile.plugins.workflow.service.DeploymentService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.RuntimeService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.WorkflowService;
import io.github.thebesteric.framework.agile.plugins.workflow.service.impl.DeploymentServiceImpl;
import io.github.thebesteric.framework.agile.plugins.workflow.service.impl.RuntimeServiceImpl;
import io.github.thebesteric.framework.agile.plugins.workflow.service.impl.WorkflowServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流引擎
 * Drools 规则引擎，Aviator 表达式引擎
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-13 16:58:59
 */
@Slf4j
public class WorkflowEngine {

    private final AgileWorkflowContext context;

    private static final List<Class<? extends BaseEntity>> ENTITY_CLASSES = List.of(
            WorkflowDefinition.class, WorkflowInstance.class,
            NodeDefinition.class, NodeAssignment.class, NodeRelation.class,
            TaskInstance.class, TaskApprove.class, TaskHistory.class
    );

    public WorkflowEngine(AgileWorkflowContext context) {
        this.context = context;
    }

    /**
     * 设置当前操作用户
     *
     * @param user 当前操作用户
     *
     * @author wangweijun
     * @since 2024/6/21 14:59
     */
    public void setCurrentUser(String user) {
        AgileWorkflowContext.setCurrentUser(user);
    }

    /**
     * 创建或更新表
     *
     * @author wangweijun
     * @since 2024/6/13 20:25
     */
    public void createOrUpdateTable() throws SQLException {
        JdbcTemplateHelper jdbcTemplateHelper = context.getJdbcTemplateHelper();
        AgileWorkflowProperties properties = context.getProperties();
        AgileWorkflowProperties.DDLAuto ddlAuto = properties.getDdlAuto();

        if (AgileWorkflowProperties.DDLAuto.CREATE == ddlAuto || AgileWorkflowProperties.DDLAuto.UPDATE == ddlAuto) {
            for (Class<? extends BaseEntity> entityClass : ENTITY_CLASSES) {
                EntityClassDomain entityClassDomain = EntityClassDomain.of(entityClass);
                // 表还没有创建过
                if (!jdbcTemplateHelper.tableExists(entityClass)) {
                    jdbcTemplateHelper.createTable(entityClassDomain);
                }
                // 表已经创建过，并且需要更新
                else if (AgileWorkflowProperties.DDLAuto.UPDATE == ddlAuto) {
                    jdbcTemplateHelper.getDatabaseMetaData(databaseMetaData -> {
                        String tableName = entityClassDomain.getTableName();
                        try {
                            Set<TableColumn> tableColumns = TableMetadataHelper.tableColumns(databaseMetaData, tableName);
                            List<ColumnDomain> columnDomains = entityClassDomain.getColumnDomains();

                            // 删除多余的列
                            Set<String> tableColumnNames = tableColumns.stream().map(TableColumn::getColumnName).collect(Collectors.toSet());
                            Set<String> entityColumnNames = columnDomains.stream().map(ColumnDomain::getName).collect(Collectors.toSet());
                            if (!entityColumnNames.containsAll(tableColumnNames)) {
                                tableColumnNames.removeAll(entityColumnNames);
                                for (String deleteColumn : tableColumnNames) {
                                    jdbcTemplateHelper.deleteColumn(databaseMetaData, tableName, deleteColumn);
                                }
                            }
                            // 添加缺失的列
                            tableColumnNames = tableColumns.stream().map(TableColumn::getColumnName).collect(Collectors.toSet());
                            entityColumnNames = columnDomains.stream().map(ColumnDomain::getName).collect(Collectors.toSet());
                            if (!tableColumnNames.containsAll(entityColumnNames)) {
                                entityColumnNames.removeAll(tableColumnNames);
                                for (String addColumn : entityColumnNames) {
                                    ColumnDomain columnDomain = columnDomains.stream().filter(column -> column.getName().equals(addColumn)).findFirst().orElse(null);
                                    if (columnDomain != null) {
                                        jdbcTemplateHelper.addColumn(columnDomain);
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            throw new WorkflowException(e);
                        }
                    });
                }
            }
        }
    }

    /**
     * 获取流程部署 Service
     *
     * @return WorkflowService
     *
     * @author wangweijun
     * @since 2024/6/13 21:25
     */
    public DeploymentService getDeploymentService() {
        return this.context.getBeanOrDefault(DeploymentService.class, new DeploymentServiceImpl(this.context));
    }

    /**
     * 获取流程 Service
     *
     * @return WorkflowService
     *
     * @author wangweijun
     * @since 2024/6/13 21:25
     */
    public WorkflowService getWorkflowService() {
        return this.context.getBeanOrDefault(WorkflowService.class, new WorkflowServiceImpl(this.context));
    }

    /**
     * 获取运行时 Service
     *
     * @return RuntimeService
     *
     * @author wangweijun
     * @since 2024/6/24 10:56
     */
    public RuntimeService getRuntimeService() {
        return this.context.getBeanOrDefault(RuntimeService.class, new RuntimeServiceImpl(this.context));
    }

}
