package io.github.thebesteric.framework.agile.plugins.logger.domain;

import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.IgnoreMethod;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.IgnoreMethods;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * IgnoreMethod
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 17:10:23
 */
@Data
public class IgnoredMethod {

    private String name;
    private Class<?>[] parameterTypes;
    private Class<?> returnType;
    private boolean nameMatch;

    private IgnoredMethod(String name, Class<?>[] parameterTypes, Class<?> returnType, boolean nameMatch) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.nameMatch = nameMatch;
    }

    public static IgnoredMethod of(String name) {
        return new IgnoredMethod(name, null, null, true);
    }

    public static IgnoredMethod of(String name, Class<?>[] parameterTypes, Class<?> returnType) {
        return of(name, parameterTypes, returnType, false);
    }

    public static IgnoredMethod of(Method method) {
        String typeName = method.getGenericReturnType().getTypeName();
        Class<?> returnType = ReflectUtils.getPrimitiveType(typeName);
        return of(method.getName(), method.getParameterTypes(), returnType);
    }

    public static IgnoredMethod of(String name, Class<?>[] parameterTypes, Class<?> returnType, boolean nameMatch) {
        return new IgnoredMethod(name, parameterTypes, returnType, nameMatch);
    }

    public static Set<IgnoredMethod> findIgnoreMethods(Class<?> clazz) {

        // 获取默认的忽略方法
        Set<IgnoredMethod> currentIgnoredMethods = IgnoredMethod.builtInIgnoredMethods();

        // 从类上查找忽略的方法
        if (clazz.isAnnotationPresent(IgnoreMethods.class)) {
            IgnoreMethods ignoreMethodsAnnotation = clazz.getAnnotation(IgnoreMethods.class);
            currentIgnoredMethods.addAll(Arrays.stream(ignoreMethodsAnnotation.value()).map(IgnoredMethod::of).collect(Collectors.toSet()));
        }

        // 从方法上查找忽略的方法
        Class<?> currentClass = clazz;
        do {
            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(IgnoreMethod.class)) {
                    currentIgnoredMethods.add(IgnoredMethod.of(method));
                }
            }
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null && currentClass != Object.class);

        // 返回该类所有需要忽略的方法
        return currentIgnoredMethods;
    }

    public static boolean matches(Method method, Set<IgnoredMethod> ignoredMethods, List<MethodMatcher> methodMatchers) {
        List<IgnoredMethod> nameMatchedMethods = ignoredMethods.stream()
                .filter(ignoredMethod -> ignoredMethod.getName().equals(method.getName())).toList();

        // 有且只有一个名称匹配的需要忽略的方法
        if (nameMatchedMethods.size() == 1 && nameMatchedMethods.get(0).isNameMatch()) {
            return true;
        }

        // 包含了需要忽略的方法，进行严格匹配
        if (nameMatchedMethods.contains(IgnoredMethod.of(method))) {
            return true;
        }

        // 检查是否有正则匹配
        for (IgnoredMethod ignoredMethod : ignoredMethods) {
            Pattern pattern = Pattern.compile(ignoredMethod.getName());
            Matcher matcher = pattern.matcher(method.getName());
            if (matcher.matches()) {
                return true;
            }
        }

        // 方法上没有 @IgnoreMethod 注解
        return methodMatchers.stream().noneMatch(methodMatcher -> methodMatcher.matcher(method));
    }

    public static Set<IgnoredMethod> builtInIgnoredMethods() {
        return new LinkedHashSet<>(Arrays.asList(IgnoredMethod.of("toString"),
                IgnoredMethod.of("equals"), IgnoredMethod.of("hashCode"), IgnoredMethod.of("clone"),
                IgnoredMethod.of("finalize"), IgnoredMethod.of("getClass"), IgnoredMethod.of("wait"),
                IgnoredMethod.of("notify"), IgnoredMethod.of("notifyAll")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IgnoredMethod that = (IgnoredMethod) o;
        return new EqualsBuilder().append(nameMatch, that.nameMatch).append(name, that.name)
                .append(parameterTypes, that.parameterTypes).append(returnType, that.returnType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(parameterTypes)
                .append(returnType).append(nameMatch).toHashCode();
    }
}
