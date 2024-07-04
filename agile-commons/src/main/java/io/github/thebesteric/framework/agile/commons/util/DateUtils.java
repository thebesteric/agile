package io.github.thebesteric.framework.agile.commons.util;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-25 11:41:49
 */
public final class DateUtils extends AbstractUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @SneakyThrows
    public static Date parse(String str, String pattern) {
        return CharSequenceUtil.isEmpty(str) ? null : new SimpleDateFormat(pattern).parse(str);
    }

    @SneakyThrows
    public static Date parseToDate(String str) {
        return parse(str, DEFAULT_DATE_FORMAT);
    }

    @SneakyThrows
    public static Date parseToDateTime(String str) {
        return parse(str, DEFAULT_DATE_TIME_FORMAT);
    }

    public static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static String formatToDate(Date date) {
        return format(date, DEFAULT_DATE_FORMAT);
    }

    public static String formatToDateTime(Date date) {
        return format(date, DEFAULT_DATE_TIME_FORMAT);
    }

}
