package io.github.thebesteric.framework.agile.plugins.logger.domain;

import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class MethodInfo {

    private String methodName;
    private String returnType;
    private LinkedHashMap<String, Object> signatures = new LinkedHashMap<>();
    private LinkedHashMap<String, Object> arguments = new LinkedHashMap<>();

    // 需要简单处理的相关类型
    private static final List<Class<?>> SIMPLE_PROCESS_CLASSES = new ArrayList<>();

    static {
        SIMPLE_PROCESS_CLASSES.add(ServletRequest.class);
        SIMPLE_PROCESS_CLASSES.add(ServletResponse.class);
        SIMPLE_PROCESS_CLASSES.add(File.class);
        SIMPLE_PROCESS_CLASSES.add(MultipartFile.class);
        SIMPLE_PROCESS_CLASSES.add(MultipartFile[].class);
        SIMPLE_PROCESS_CLASSES.add(InputStream.class);
        SIMPLE_PROCESS_CLASSES.add(OutputStream.class);
        SIMPLE_PROCESS_CLASSES.add(Resource.class);
    }

    public MethodInfo(Method method, Object[] args) {
        this.methodName = method.getName();
        Parameter[] params = method.getParameters();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                // Add signatures
                signatures.put(param.getName(), param.getParameterizedType().getTypeName());

                // Processing args
                if (args != null) {
                    if (shouldSimpleProcess(param)) {
                        if (args[i] instanceof MultipartFile file) {
                            arguments.put(param.getName(), file.getOriginalFilename());
                        } else if (args[i] instanceof File file) {
                            arguments.put(param.getName(), file.getName());
                        } else if (args[i] instanceof Resource resource) {
                            arguments.put(param.getName(), resource.getFilename());
                        } else {
                            arguments.put(param.getName(), param.getParameterizedType().getTypeName());
                        }
                    } else {
                        if (args[i] instanceof Exception) {
                            arguments.put(param.getName(), String.valueOf(args[i]));
                        } else {
                            Object argValue = args[i];
                            if (argValue instanceof Collection<?> collection) {
                                for (Object obj : collection) {
                                    if (obj != null && shouldSimpleProcess(param)) {
                                        if (obj instanceof MultipartFile file) {
                                            argValue = file.getOriginalFilename();
                                        } else if (obj instanceof File file) {
                                            argValue = file.getName();
                                        } else if (obj instanceof Resource resource) {
                                            argValue = resource.getFilename();
                                        } else {
                                            argValue = param.getParameterizedType().getTypeName();
                                        }
                                        break;
                                    }
                                }
                            }
                            arguments.put(param.getName(), argValue);
                        }
                    }
                }
            }
        }
        this.returnType = method.getReturnType().getName();
    }

    // 添加需要简单处理的相关类型
    public static void addSimpleProcessClass(Class<?> clazz) {
        SIMPLE_PROCESS_CLASSES.add(clazz);
    }

    // 是否需要简单处理
    private boolean shouldSimpleProcess(Parameter param) {

        // 如果 param 是列表类型，则取出其泛型类型，供后续检查
        if (Collection.class.isAssignableFrom(param.getType())) {
            // 获取泛型类型
            Type genericType = param.getParameterizedType();
            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    Type actualType = actualTypeArguments[0];
                    if (actualType instanceof Class<?> actualClass) {
                        for (Class<?> clazz : SIMPLE_PROCESS_CLASSES) {
                            if (clazz.isAssignableFrom(actualClass)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        // 直接检查参数类型
        else {
            for (Class<?> clazz : SIMPLE_PROCESS_CLASSES) {
                if (clazz.isAssignableFrom(param.getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    @SneakyThrows
    public String toString() {
        return JsonUtils.MAPPER.writeValueAsString(this);
    }
}