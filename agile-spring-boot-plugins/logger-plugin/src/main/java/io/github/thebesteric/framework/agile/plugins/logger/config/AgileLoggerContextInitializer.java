package io.github.thebesteric.framework.agile.plugins.logger.config;

import io.github.thebesteric.framework.agile.commons.util.ClassPathUtils;
import io.github.thebesteric.framework.agile.core.scaner.ClassPathScanner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * AgileLoggerContextInitializer
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 12:04:13
 */
@RequiredArgsConstructor
public class AgileLoggerContextInitializer implements ApplicationContextAware {

    private final List<ClassPathScanner> scanners;

    public void initialize() {
        String projectPath = ClassPathUtils.getProjectPath();
        List<String> compilePaths = ClassPathUtils.compilePaths;

        // Scanner @Controller and @AgileLogger and so on
        for (ClassPathScanner classPathScanner : scanners) {
            classPathScanner.scan(projectPath, compilePaths);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        initialize();
    }
}
