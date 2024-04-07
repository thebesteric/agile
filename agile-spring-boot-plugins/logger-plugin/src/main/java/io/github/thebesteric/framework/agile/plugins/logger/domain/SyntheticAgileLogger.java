package io.github.thebesteric.framework.agile.plugins.logger.domain;


import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>Merge @AgileLogger on Class and @AgileLogger on Method.
 * When the same attribute exists on both class and method, the attribute of method takes effect
 *
 * @author Eric Joe
 * @version 1.0
 * @see io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger
 * @since 2022-08-05 23:45:22
 */
@Data
public class SyntheticAgileLogger {

    protected Method method;
    protected String tag;
    protected String extra;
    protected LogLevel level;
    protected String exception;
    protected String[] ignoreMethods;
    protected boolean matched = true;

    private static final Map<String, SyntheticAgileLogger> CACHE = new HashMap<>(128);

    public SyntheticAgileLogger(Method method) {
        this(method, InvokeLog.DEFAULT_TAG, InvokeLog.DEFAULT_LOG_LEVEL);
        setComponentTag(method.getDeclaringClass());
    }

    public SyntheticAgileLogger(Method method, String defaultTag) {
        this(method, defaultTag, InvokeLog.DEFAULT_LOG_LEVEL);
    }

    public SyntheticAgileLogger(Method method, String defaultTag, LogLevel defaultLevel) {
        AgileLogger onType = method.getDeclaringClass().getAnnotation(AgileLogger.class);
        AgileLogger onMethod = method.getAnnotation(AgileLogger.class);

        String tagOnType = null, tagOnMethod = null;
        String extraOnType = null, extraOnMethod = null;
        String levelOnType = null, levelOnMethod = null;
        List<String> ignoreMethodsOnType = null, ignoreMethodsOnMethod = null, mergedIgnoreMethods = new ArrayList<>();

        if (onType != null) {
            tagOnType = onType.tag();
            extraOnType = onType.extra();
            levelOnType = onType.level();
            ignoreMethodsOnType = List.of(onType.ignoreMethods());
        }
        if (onMethod != null) {
            tagOnMethod = onMethod.tag();
            extraOnMethod = onMethod.extra();
            levelOnMethod = onMethod.level();
            ignoreMethodsOnMethod = List.of(onMethod.ignoreMethods());
        }

        // Set method attribute
        this.method = method;

        // Merge ignore methods
        if (ignoreMethodsOnType != null) {
            mergedIgnoreMethods.addAll(ignoreMethodsOnType);
        }
        if (ignoreMethodsOnMethod != null) {
            mergedIgnoreMethods.addAll(ignoreMethodsOnMethod);
        }

        // Merge attributes
        this.tag = StringUtils.blankToNull(StringUtils.notEquals(defaultTag, tagOnMethod) ?
                (tagOnMethod != null ? tagOnMethod : tagOnType) : (tagOnType == null ? defaultTag : tagOnType));
        this.level = StringUtils.notEquals(defaultLevel.name(), levelOnMethod) ?
                (levelOnMethod != null ? LogLevel.get(levelOnMethod) : LogLevel.get(levelOnType)) : (levelOnType == null ? defaultLevel : LogLevel.get(levelOnType));
        this.extra = StringUtils.blankToNull(StringUtils.isNotEmpty(extraOnMethod) ? extraOnMethod : extraOnType);
        this.ignoreMethods = new HashSet<>(mergedIgnoreMethods).toArray(new String[0]);

        // Has not annotated @AgileLogger
        if (onType == null && onMethod == null) {
            this.level = defaultLevel;
            this.tag = defaultTag;
            this.matched = false;
        }
    }

    public static SyntheticAgileLogger buildSyntheticAgileLogger(Method method) {
        String key = ReflectUtils.methodSignature(method);
        SyntheticAgileLogger cachedSyntheticAgileLogger = CACHE.get(key);

        // When level == ERROR && exception == null then retryOnErrorWithoutException is true else false
        boolean retryOnErrorWithoutException = false;
        if (cachedSyntheticAgileLogger != null) {
            String exception = cachedSyntheticAgileLogger.getException();
            retryOnErrorWithoutException = (LogLevel.ERROR == cachedSyntheticAgileLogger.level) && StringUtils.isNotEmpty(exception);
        }
        // 重新加入缓存
        if (cachedSyntheticAgileLogger == null || retryOnErrorWithoutException) {
            synchronized (SyntheticAgileLogger.class) {
                cachedSyntheticAgileLogger = new SyntheticAgileLogger(method);
                CACHE.put(key, cachedSyntheticAgileLogger);
            }
        }
        return cachedSyntheticAgileLogger;
    }

    private void setComponentTag(Class<?> type) {
        if (this.tag == null || this.tag.equals(InvokeLog.DEFAULT_TAG)) {
            if (ReflectUtils.anyAnnotationPresent(type, RestController.class, Controller.class)) {
                this.tag = Controller.class.getName();
            } else if (ReflectUtils.isAnnotationPresent(type, Service.class)) {
                this.tag = Service.class.getName();
            } else if (ReflectUtils.isAnnotationPresent(type, Repository.class)) {
                this.tag = Repository.class.getName();
            } else if (ReflectUtils.isAnnotationPresent(type, Component.class)) {
                this.tag = Component.class.getName();
            } else {
                this.tag = InvokeLog.DEFAULT_TAG;
            }
        }
    }
}
