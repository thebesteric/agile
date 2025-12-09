package io.github.thebesteric.framework.agile.plugins.workflow.domain;

import cn.hutool.json.JSONUtil;
import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BusinessInfo
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-11-05 19:30:03
 */
@Data
public class BusinessInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -7887802703132244350L;

    @Schema(description = "业务类名")
    private String className;
    @Schema(description = "业务对象")
    private Object business;

    public static BusinessInfo of(Class<?> clazz, Object business) {
        BusinessInfo businessInfo = new BusinessInfo();
        businessInfo.setClassName(clazz.getName());
        businessInfo.setBusiness(business);
        return businessInfo;
    }

    public static BusinessInfo of(Object business) {
        return BusinessInfo.of(business.getClass(), business);
    }

    public Object getObject() {
        try {
            Class<?> aClass = Class.forName(className);
            return this.getObject(aClass);
        } catch (ClassNotFoundException e) {
            return this.getBusiness();
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> clazz) {
        if (ReflectUtils.isPrimitiveOrWarp(clazz)) {
            return clazz.getDeclaredConstructor(String.class).newInstance(business.toString());
        } else if (ReflectUtils.isStringType(clazz)) {
            return (T) business;
        } else if (Map.class.isAssignableFrom(clazz) && business instanceof Map) {
            return (T) business;
        } else if (List.class.isAssignableFrom(clazz) && business instanceof List) {
            return (T) business;
        } else if (Set.class.isAssignableFrom(clazz) && business instanceof Set) {
            return (T) business;
        }
        return JSONUtil.toBean(business.toString(), clazz);
    }

    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
