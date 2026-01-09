package io.github.thebesteric.framework.agile.plugins.logger.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.commons.util.StringUtils;
import io.github.thebesteric.framework.agile.core.domain.R;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;


/**
 * InvokeLog
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-07-27 12:15:13
 */
@Data
public class InvokeLog {

    public static final String DEFAULT_TAG = "default";
    public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;
    private static final Object NULL_OBJECT_MARKER = null;

    /** 日志 ID */
    protected String logId;

    /** 日志 ID */
    protected String logParentId;

    /** 日志标签 */
    protected String tag = DEFAULT_TAG;

    /** 日志级别 */
    protected LogLevel level = DEFAULT_LOG_LEVEL;

    /** 链路 ID */
    protected String trackId;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createdAt = new Date();

    /** 执行信息 */
    protected ExecuteInfo executeInfo;

    /** 执行结果 */
    protected Object result;

    /** 异常类 */
    protected Class<?> exceptionClass;

    /** 异常信息 */
    protected String exception;

    /** 扩展信息 */
    protected Object extra;

    /** 线程名称 */
    protected String threadName = Thread.currentThread().getName();

    /** 是否 Mock 数据 */
    protected boolean mock = false;

    public InvokeLog() {
        this.logId = AgileLoggerContext.idGenerator.generate();
    }

    public InvokeLog(String logParentId) {
        this();
        this.logParentId = logParentId;
    }

    public InvokeLog(String logId, String logParentId) {
        this(logParentId);
        this.logId = logId;
    }

    public static Builder builder(InvokeLog invokeLog) {
        return new Builder(invokeLog);
    }

    public static Builder builder() {
        return new Builder(new InvokeLog());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public String print() {
        Object resultObj = this.getResult();
        // 对象直接就是流对象
        if (isStreamObject(resultObj)) {
            this.setResult(NULL_OBJECT_MARKER);
        }
        // 对象不是流对象
        else {
            // 尝试处理 R 对象
            if (resultObj instanceof R rObjResult) {
                Object data = rObjResult.getData();
                if (isStreamObject(data)) {
                    rObjResult.setData(NULL_OBJECT_MARKER);
                } else {
                    // 继续处理 R 对象的 data 字段，判断是否有流对象字段
                    Object processedData = processObjectFields(data);
                    if (processedData != data) {
                        rObjResult.setData(processedData);
                    }
                }
            }
            // 尝试处理普通对象
            else {
                // 遍历普通对象的字段，判断是否有流对象字段
                Object processedResult = processObjectFields(resultObj);
                if (processedResult != resultObj) {
                    this.setResult(processedResult);
                }
            }
        }
        return this.toString();
    }

    /**
     * 递归处理对象字段，将流对象替换为标记
     *
     * @param obj 待处理的对象
     *
     * @return 处理后的对象
     */
    private Object processObjectFields(Object obj) {
        if (obj == null) {
            return null;
        }

        // 如果是流对象，直接返回标记
        if (isStreamObject(obj)) {
            return NULL_OBJECT_MARKER;
        }

        // 如果是基本类型或包装类、字符串，直接返回
        if (isPrimitiveOrWrapper(obj)) {
            return obj;
        }

        // 如果是集合类型，递归处理集合元素
        if (obj instanceof Collection<?> collection) {
            List<Object> processedList = new ArrayList<>();
            for (Object item : collection) {
                processedList.add(processObjectFields(item));
            }
            return processedList;
        }

        // 如果是 Map 类型，递归处理 Map 的值
        if (obj instanceof Map<?, ?> map) {
            Map<Object, Object> processedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                processedMap.put(entry.getKey(), processObjectFields(entry.getValue()));
            }
            return processedMap;
        }

        // 如果是数组类型，递归处理数组元素
        if (obj.getClass().isArray()) {
            Object[] array = (Object[]) obj;
            Object[] processedArray = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                processedArray[i] = processObjectFields(array[i]);
            }
            return processedArray;
        }

        // 对于普通对象，使用反射遍历字段
        try {
            Class<?> clazz = obj.getClass();
            // 跳过 Java 内置类和常见库类
            if (clazz.getName().startsWith("java.") || clazz.getName().startsWith("javax.")) {
                return obj;
            }

            // 获取所有字段
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                // 跳过静态字段和 final 字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                Object fieldValue = field.get(obj);

                if (fieldValue != null) {
                    // 检查字段值是否为流对象
                    if (isStreamObject(fieldValue)) {
                        // 将流对象字段设置为 null，避免类型不匹配异常
                        field.set(obj, NULL_OBJECT_MARKER);
                    }
                    // 递归处理复杂对象字段
                    else if (!isPrimitiveOrWrapper(fieldValue)) {
                        Object processedFieldValue = processObjectFields(fieldValue);
                        if (processedFieldValue != fieldValue) {
                            field.set(obj, processedFieldValue);
                        }
                    }
                }
            }

            return obj;
        } catch (Exception e) {
            // 如果反射处理失败，返回原对象
            LoggerPrinter.error("Process stream object error.", e);
            return obj;
        }
    }

    /**
     * 判断对象是否为流对象
     */
    private static boolean isStreamObject(Object obj) {
        return obj instanceof InputStream || obj instanceof OutputStream
               || obj instanceof Reader || obj instanceof Writer
               || obj instanceof Resource || obj instanceof MultipartFile;
    }

    /**
     * 判断对象是否为基本类型或包装类
     *
     * @param obj 待判断的对象
     *
     * @return 是否为基本类型或包装类
     */
    private boolean isPrimitiveOrWrapper(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean ||
               obj instanceof Character || obj instanceof Date ||
               obj.getClass().isPrimitive();
    }

    @Override
    @SneakyThrows
    public String toString() {
        return JsonUtils.MAPPER.writeValueAsString(this);
    }

    public static class Builder {

        private final InvokeLog invokeLog;

        public Builder(InvokeLog invokeLog) {
            this.invokeLog = invokeLog;
        }

        public Builder id(String id) {
            this.invokeLog.logId = id;
            return this;
        }

        public String id() {
            return this.invokeLog.logId;
        }

        public Builder parentId(String parentId) {
            this.invokeLog.logParentId = parentId;
            return this;
        }

        public String parentId() {
            return this.invokeLog.logParentId;
        }

        public Builder tag(String tag) {
            this.invokeLog.tag = tag;
            return this;
        }

        public Builder level(LogLevel level) {
            this.invokeLog.level = level;
            return this;
        }

        public Builder trackId(String trackId) {
            this.invokeLog.trackId = trackId;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.invokeLog.createdAt = createdAt;
            return this;
        }

        public Builder createdAt(long timestamp) {
            return this.createdAt(new Date(timestamp));
        }

        public Builder executeInfo(ExecuteInfo executeInfo) {
            this.invokeLog.executeInfo = executeInfo;
            return this;
        }

        public Builder result(Object result) {
            this.invokeLog.result = result;
            return this;
        }

        public Builder exception(String exception) {
            this.invokeLog.exception = exception;
            return this;
        }

        public Builder exceptionClass(Class<?> exceptionClass) {
            this.invokeLog.exceptionClass = exceptionClass;
            return this;
        }

        public Builder extra(String extra) {
            this.invokeLog.extra = StringUtils.blankToNull(extra);
            return this;
        }

        public Builder threadName(String threadName) {
            this.invokeLog.threadName = threadName;
            return this;
        }

        public Builder mock(boolean mock) {
            this.invokeLog.mock = mock;
            return this;
        }

        public InvokeLog build() {
            if (this.invokeLog.createdAt == null) {
                this.invokeLog.createdAt = new Date();
            }
            if (this.invokeLog.threadName == null) {
                this.invokeLog.threadName = Thread.currentThread().getName();
            }
            return this.invokeLog;
        }
    }
}
