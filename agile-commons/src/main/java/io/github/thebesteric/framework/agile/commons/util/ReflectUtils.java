package io.github.thebesteric.framework.agile.commons.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

/**
 * ReflectUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-08 15:05:24
 */
public class ReflectUtils extends AbstractUtils {

    private static final Map<String, Class<?>> PRIMITIVE_TYPE_MAP = new HashMap<>();


    static {
        PRIMITIVE_TYPE_MAP.put("void", Void.TYPE);
        PRIMITIVE_TYPE_MAP.put("char", Character.TYPE);
        PRIMITIVE_TYPE_MAP.put("byte", Byte.TYPE);
        PRIMITIVE_TYPE_MAP.put("short", Short.TYPE);
        PRIMITIVE_TYPE_MAP.put("int", Integer.TYPE);
        PRIMITIVE_TYPE_MAP.put("long", Long.TYPE);
        PRIMITIVE_TYPE_MAP.put("float", Float.TYPE);
        PRIMITIVE_TYPE_MAP.put("double", Double.TYPE);
        PRIMITIVE_TYPE_MAP.put("boolean", Boolean.TYPE);
    }

    public static Class<?> getPrimitiveType(String typeName) {
        if (typeName.startsWith("org.springframework") || typeName.startsWith("java.util")) {
            return null;
        }
        Class<?> type = PRIMITIVE_TYPE_MAP.get(typeName);
        if (type == null) {
            try {
                type = Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                LoggerPrinter.debug(e.getMessage());
            }
        }
        return type;
    }

    public static <T extends Annotation> T getAnnotation(Class<?> objectClass, Class<T> annotationClass) {
        return objectClass.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    public static boolean isAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass) {
        return isAnnotationPresent(objectClass, annotationClass, false);
    }

    public static boolean isAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass, boolean typeAndMethod) {
        if (allAnnotationPresent(objectClass, annotationClass)) {
            return true;
        } else if (typeAndMethod) {
            for (Method method : objectClass.getDeclaredMethods()) {
                if (allAnnotationPresent(method, annotationClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    @SafeVarargs
    public static boolean anyAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass, Class<? extends Annotation>... annotationClasses) {
        List<Class<? extends Annotation>> annoClasses = mergeIndefiniteParams(annotationClass, annotationClasses);
        return annoClasses.stream().anyMatch(objectClass::isAnnotationPresent);
    }

    @SafeVarargs
    public static boolean anyAnnotationPresent(Method method, Class<? extends Annotation> annotationClass, Class<? extends Annotation>... annotationClasses) {
        List<Class<? extends Annotation>> annoClasses = mergeIndefiniteParams(annotationClass, annotationClasses);
        return annoClasses.stream().anyMatch(method::isAnnotationPresent);
    }

    @SafeVarargs
    public static boolean allAnnotationPresent(Class<?> objectClass, Class<? extends Annotation> annotationClass, Class<? extends Annotation>... annotationClasses) {
        List<Class<? extends Annotation>> annoClasses = mergeIndefiniteParams(annotationClass, annotationClasses);
        return annoClasses.stream().allMatch(objectClass::isAnnotationPresent);
    }

    @SafeVarargs
    public static boolean allAnnotationPresent(Method method, Class<? extends Annotation> annotationClass, Class<? extends Annotation>... annotationClasses) {
        List<Class<? extends Annotation>> annoClasses = mergeIndefiniteParams(annotationClass, annotationClasses);
        return annoClasses.stream().allMatch(method::isAnnotationPresent);
    }

    public static String[] getModifiers(Class<?> clazz) {
        return Modifier.toString(clazz.getModifiers()).split(" ");
    }

    public static String[] getModifiers(Member member) {
        return Modifier.toString(member.getModifiers()).split(" ");
    }

    public static String methodSignature(Method method) {
        String modifiers = StringUtils.join(getModifiers(method), " ");
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            args.append(method.getParameterTypes()[i].getName());
            if (i < method.getParameterTypes().length - 1)
                args.append(",");
        }
        return modifiers + " " + method.getDeclaringClass().getName() + "#" + method.getName() + "(" + args + ")";
    }

    @SafeVarargs
    private static <T> List<T> mergeIndefiniteParams(T t, T... indefiniteParams) {
        List<T> list = new ArrayList<>();
        list.add(t);
        if (indefiniteParams != null && indefiniteParams.length > 0) {
            list.addAll(Arrays.asList(indefiniteParams));
        }
        return list;
    }

    public static List<Field> getFields(Class<?> clazz, Predicate<Field> predicate) {
        List<Field> fields = new ArrayList<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (predicate == null || predicate.test(field)) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static boolean isPrivate(Class<?> clazz) {
        return Modifier.isPrivate(clazz.getModifiers());
    }

    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    public static boolean isProtected(Class<?> clazz) {
        return Modifier.isProtected(clazz.getModifiers());
    }

    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    public static boolean isStatic(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers());
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    public static boolean isTransient(Class<?> clazz) {
        return Modifier.isTransient(clazz.getModifiers());
    }

    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    public static boolean isPrimitive(Field field) {
        return isPrimitive(field.getType());
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    public static boolean isPrimitiveOrWarp(Field field) {
        return isPrimitiveOrWarp(field.getType());
    }

    public static boolean isPrimitiveOrWarp(Class<?> clazz) {
        try {
            return clazz.isPrimitive() || ((Class<?>) clazz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception ignore) {
            return false;
        }
    }

    public static boolean isPrimitiveOrWarp(Object obj) {
        return isPrimitiveOrWarp(obj.getClass());
    }

    public static boolean isListType(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    public static boolean isListType(Field field) {
        Class<?> type = field.getType();
        return isListType(type);
    }

    public static boolean isMapType(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    public static boolean isMapType(Field field) {
        Class<?> type = field.getType();
        return isMapType(type);
    }

    public static boolean isArrayType(Class<?> clazz) {
        return clazz.isArray();
    }

    public static boolean isArrayType(Field field) {
        Class<?> type = field.getType();
        return isArrayType(type);
    }

    public static boolean isStringType(Class<?> clazz) {
        return String.class == clazz;
    }

    public static boolean isStringType(Field field) {
        Class<?> type = field.getType();
        return isStringType(type);
    }

}
