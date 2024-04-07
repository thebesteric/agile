package io.github.thebesteric.framework.agile.commons.util;

import java.lang.reflect.Constructor;

/**
 * ClassUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022/7/25
 */
public class ClassUtils extends AbstractUtils {

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignore) {
        }

        if (cl == null) {
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ignore) {
                }
            }
        }
        return cl;
    }

    public static Class<?> forName(String className) {
        return forName(className, true);
    }

    public static Class<?> forName(String className, boolean initialize) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, initialize, getDefaultClassLoader());
        } catch (ClassNotFoundException ignore) {
        }
        return clazz;
    }

    public static Object newInstance(String className, Object... args) {
        Class<?> clazz = forName(className);
        return newInstance(clazz, args);
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        Object obj = null;
        try {
            Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
            obj = declaredConstructor.newInstance(args);
        } catch (Exception ignore) {
        }
        return obj;
    }

    public static String getPackageName(Class<?> clazz) {
        assert clazz != null : "Class must not be null";
        return getPackageName(clazz.getName());
    }

    public static String getPackageName(String className) {
        assert className != null : "Class name must not be null";
        int lastDotIndex = className.lastIndexOf(46);
        return lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "";
    }

}
