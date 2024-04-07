package io.github.thebesteric.framework.agile.plugins.logger.domain;

import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.plugins.logger.annotation.AgileLogger;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * AgileLoggerHelper
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-14 11:24:30
 */
@Getter
public class AgileLoggerHelper {

    private String tag;
    private final String extra;
    private final String level;

    public AgileLoggerHelper(Method method) {
        AgileLogger onType = method.getDeclaringClass().getAnnotation(AgileLogger.class);
        AgileLogger onMethod = method.getAnnotation(AgileLogger.class);

        String tagOnType = null, tagOnMethod = null;
        String extraOnType = null, extraOnMethod = null;
        String levelOnType = null, levelOnMethod = null;

        if (onType != null) {
            tagOnType = onType.tag();
            extraOnType = onType.extra();
            levelOnType = onType.level();
        }
        if (onMethod != null) {
            tagOnMethod = onMethod.tag();
            extraOnMethod = onMethod.extra();
            levelOnMethod = onMethod.level();
        }

        String defaultTag = InvokeLog.DEFAULT_TAG;
        String defaultLevel = LogLevel.INFO.name();

        if (StringUtils.notEquals(defaultTag, tagOnMethod, true)) {
            this.tag = StringUtils.blankToNull(tagOnMethod != null ? tagOnMethod : tagOnType);
        } else {
            this.tag = StringUtils.blankToNull(tagOnType != null ? tagOnType : defaultTag);
        }
        if (this.tag == null || this.tag.equalsIgnoreCase(InvokeLog.DEFAULT_TAG)) {
            this.tag = initTag(method.getDeclaringClass());
        }


        if (StringUtils.notEquals(defaultLevel, levelOnMethod, true)) {
            this.level = StringUtils.blankToNull(levelOnMethod != null ? levelOnMethod : levelOnType);
        } else {
            this.level = StringUtils.blankToNull(levelOnType != null ? levelOnType : defaultLevel);
        }

        if (StringUtils.isNotEmpty(extraOnMethod)) {
            this.extra = StringUtils.blankToNull(extraOnMethod);
        } else {
            this.extra = StringUtils.blankToNull(extraOnType);
        }
    }

    private String initTag(Class<?> clazz) {
        if (clazz.isAnnotationPresent(RestController.class) || clazz.isAnnotationPresent(Controller.class)) {
            return  "controller";
        } else if (clazz.isAnnotationPresent(Service.class)) {
            return "service";
        } else if (clazz.isAnnotationPresent(Repository.class)) {
            return "repository";
        } else if (clazz.isAnnotationPresent(Component.class)) {
            return "component";
        }
        return InvokeLog.DEFAULT_TAG;
    }
}
