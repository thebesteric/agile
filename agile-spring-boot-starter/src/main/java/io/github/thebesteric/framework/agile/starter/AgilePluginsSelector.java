package io.github.thebesteric.framework.agile.starter;

import io.github.thebesteric.framework.agile.commons.constant.AgilePlugins;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * AgilePluginsSelector
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 16:01:37
 */
@Slf4j
public class AgilePluginsSelector implements ImportSelector {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    @NonNull
    @Override
    public String[] selectImports(@NonNull AnnotationMetadata metadata) {
        return loadPlugins();
    }

    /**
     * 插件加载
     */
    private String[] loadPlugins() {
        List<String> list = new ArrayList<>();

        AgilePlugins[] plugins = AgilePlugins.values();
        for (AgilePlugins plugin : plugins) {
            try {
                String className = plugin.getClassName();
                Class.forName(className);
                list.add(className);
            } catch (ClassNotFoundException e) {
                loggerPrinter.debug("{} is not installed", plugin.getName());
            }
        }

        return list.toArray(new String[0]);
    }
}
