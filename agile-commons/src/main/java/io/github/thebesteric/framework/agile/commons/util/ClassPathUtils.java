package io.github.thebesteric.framework.agile.commons.util;

import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassPathUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022/7/25
 */
public class ClassPathUtils extends AbstractUtils  {

    @Getter
    public static List<String> compilePaths = new ArrayList<>();

    static {
        compilePaths.add("target" + File.separator + "classes");
        compilePaths.add("target" + File.separator + "test-classes");
        compilePaths.add("build" + File.separator + "classes" + File.separator + "java" + File.separator + "main");
        compilePaths.add("build" + File.separator + "test-classes" + File.separator + "java" + File.separator + "main");
    }

    public static String getProjectPath() {
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        if (defaultClassLoader != null) {
            String path = Objects.requireNonNull(defaultClassLoader.getResource("")).getPath();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                path = path.substring(1);
            }
            for (String compilePath : compilePaths) {
                path = path.replace("%20", " ").replace(compilePath + File.separator, "");
            }
            return path;
        }
        return null;
    }

}
