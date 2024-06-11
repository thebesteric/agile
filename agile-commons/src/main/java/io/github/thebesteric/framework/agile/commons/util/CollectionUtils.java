package io.github.thebesteric.framework.agile.commons.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CollectionUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-05 18:03:57
 */
public class CollectionUtils extends AbstractUtils {

    public static boolean isArray(Object obj) {
        return null != obj && obj.getClass().isArray();
    }

    public static boolean isMap(Object obj) {
        return obj instanceof Map;
    }

    public static boolean isList(Object obj) {
        return obj instanceof List;
    }

    public static boolean isSet(Object obj) {
        return obj instanceof Set;
    }

    public static boolean isEmpty(Object[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(boolean[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(char[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(byte[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(short[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(int[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(long[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(float[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isEmpty(double[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(boolean[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(char[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(byte[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(short[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(int[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(long[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(float[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isNotEmpty(double[] arr) {
        return !isEmpty(arr);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    @SafeVarargs
    public static <T> List<T> createList(T... items) {
        if (items == null || items.length == 0) return new ArrayList<>();
        return Stream.of(items).collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> Set<T> createSet(T... items) {
        if (items == null || items.length == 0) return new HashSet<>();
        return Stream.of(items).collect(Collectors.toSet());
    }

    public static <T> List<T> getList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    public static <T> Set<T> getSet(Set<T> set) {
        return set == null ? new HashSet<>() : set;
    }

    public static <T> boolean isEquals(List<T> list1, List<T> list2) {
        Set<T> set1 = new HashSet<>(list1);
        Set<T> set2 = new HashSet<>(list2);
        if (set1.size() != set2.size()) {
            return false;
        }
        return !set1.addAll(set2);
    }

    public static  <T> boolean isStrictEquals(List<T> list1, List<T> list2) {
        return list1.equals(list2);
    }

    public static String toString(Object obj) {
        return ArrayUtils.toString(obj);
    }

}
