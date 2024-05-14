package io.github.thebesteric.framework.agile.plugins.database.config;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.domain.PackageFinder;
import io.github.thebesteric.framework.agile.core.scaner.AnnotationTypeCandidateComponentScanner;
import io.github.thebesteric.framework.agile.plugins.database.annotation.EntityClass;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

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

    public AgileDatabaseContext(ApplicationContext applicationContext, AgileDatabaseProperties properties) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        List<String> packageNames = PackageFinder.getPackageNames();
        List<Class<? extends Annotation>> classes = List.of(EntityClass.class, TableName.class);
        AnnotationTypeCandidateComponentScanner scanner = new AnnotationTypeCandidateComponentScanner(packageNames, classes);
        Set<String> entityClassNames = scanner.scan();
        if (entityClassNames != null) {
            for (String entityClassName : entityClassNames) {
                Try.of(() -> entityClasses.add(Class.forName(entityClassName)));
            }
        }
    }

}
