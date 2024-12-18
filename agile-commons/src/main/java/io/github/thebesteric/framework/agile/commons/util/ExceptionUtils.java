package io.github.thebesteric.framework.agile.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ExceptionUtils
 *
 * @author Eric Joe
 * @since 1.0
 */
public class ExceptionUtils extends AbstractUtils {

    public static String getSimpleMessage(Throwable ex, int limit) {
        return StringUtils.limit(getSimpleMessage(ex), limit);
    }

    public static String getSimpleMessage(Throwable ex) {
        if (ex != null) {
            String exTitle = getTitle(ex);
            StackTraceElement exCause = getMajorCause(ex);
            return exTitle + (exCause == null ? "" : ": " + exCause);
        }
        return null;
    }

    public static String getTitle(Throwable ex) {
        String className = ex.getClass().getName();
        return StringUtils.isNotEmpty(ex.getMessage()) ? className + ": " + ex.getMessage() : ex.toString();
    }

    public static StackTraceElement[] getCauses(Throwable ex) {
        return ex.getStackTrace();
    }

    public static StackTraceElement getMajorCause(Throwable ex) {
        StackTraceElement[] causes = getCauses(ex);
        return CollectionUtils.isEmpty(causes) ? null : causes[0];
    }

    public static StringWriter getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw;
    }

    public static String getStackTraceStr(Throwable ex) {
        return getStackTrace(ex).toString();
    }

}
