package io.github.thebesteric.framework.agile.plugins.logger.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Date;

@Data
public class ExecuteInfo {

    private String className;
    private MethodInfo methodInfo;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createdAt;

    private long duration;

    public ExecuteInfo() {
        this.createdAt = new Date();
    }

    public ExecuteInfo(String className, MethodInfo methodInfo) {
        this();
        this.className = className;
        this.methodInfo = methodInfo;
    }

    public ExecuteInfo(Method method, Object[] args) {
        this(method.getDeclaringClass().getName(), new MethodInfo(method, args));
    }

    @Override
    @SneakyThrows
    public String toString() {
        return JsonUtils.MAPPER.writeValueAsString(this);
    }
}