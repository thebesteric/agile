package io.github.thebesteric.framework.agile.plugins.database.config;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.commons.exception.DuplicateParamsException;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.scaner.AnnotationTypeCandidateComponentScanner;
import io.github.thebesteric.framework.agile.plugins.database.core.annotation.EntityClass;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableCreateListener;
import io.github.thebesteric.framework.agile.plugins.database.core.listener.TableUpdateListener;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AgileDatabaseContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-13 11:22:56
 */
@Getter
@Slf4j
public class AgileDatabaseContext extends AbstractAgileContext {

    private final Set<Class<?>> entityClasses = new HashSet<>();
    private final AgileDatabaseProperties properties;
    private final List<TableCreateListener> tableCreateListeners;
    private final List<TableUpdateListener> tableUpdateListeners;

    public AgileDatabaseContext(ApplicationContext applicationContext, AgileDatabaseProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        findEntityClasses();
        this.tableCreateListeners = this.getBeans(TableCreateListener.class);
        this.tableUpdateListeners = this.getBeans(TableUpdateListener.class);
    }

    private void findEntityClasses() {
        List<String> packageNames = PackageFinder.getPackageNames();
        List<Class<? extends Annotation>> classes = List.of(EntityClass.class, TableName.class);
        AnnotationTypeCandidateComponentScanner scanner = new AnnotationTypeCandidateComponentScanner(packageNames, classes);
        Set<String> entityClassNames = scanner.scan();
        if (entityClassNames != null) {
            Set<String> tableNames = new HashSet<>();
            for (String entityClassName : entityClassNames) {
                Class<?> clazz = Try.of(() -> Class.forName(entityClassName)).get();
                // 不需要创建表的实体类
                EntityClass entityClassAnno = clazz.getAnnotation(EntityClass.class);
                if (entityClassAnno != null && entityClassAnno.ignore()) {
                    LoggerPrinter.info("Ignore entity class: {}", entityClassName);
                    continue;
                }
                // 获取表名
                String tableName = null;
                if (entityClassAnno != null) {
                    tableName = entityClassAnno.value();
                }
                if (CharSequenceUtil.isEmpty(tableName)) {
                    TableName tableNameAnno = clazz.getAnnotation(TableName.class);
                    if (tableNameAnno != null) {
                        tableName = tableNameAnno.value();
                    }
                }
                if (CharSequenceUtil.isEmpty(tableName)) {
                    String simpleClassName = clazz.getSimpleName();
                    tableName = Introspector.decapitalize(simpleClassName);
                }
                // 判断表名是否重复
                if (tableNames.contains(tableName)) {
                    throw new DuplicateParamsException("Duplicate tableName exception: " + tableName);
                }
                tableNames.add(tableName);
                // 加入集合
                entityClasses.add(clazz);
            }
        }
    }

}
