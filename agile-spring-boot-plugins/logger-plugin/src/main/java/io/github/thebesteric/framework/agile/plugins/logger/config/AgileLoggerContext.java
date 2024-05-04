package io.github.thebesteric.framework.agile.plugins.logger.config;

import io.github.thebesteric.framework.agile.core.config.AbstractAgileContext;
import io.github.thebesteric.framework.agile.core.generator.DefaultIdGenerator;
import io.github.thebesteric.framework.agile.core.generator.IdGenerator;
import io.github.thebesteric.framework.agile.core.matcher.clazz.ClassMatcher;
import io.github.thebesteric.framework.agile.core.matcher.method.MethodMatcher;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.impl.LogRecorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.RequestLoggerProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.impl.DefaultRequestLoggerProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.response.ResponseSuccessDefineProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.response.impl.DefaultResponseSuccessDefineProcessorProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * AgileLoggerContext
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:01:09
 */
@Getter
@Slf4j
public class AgileLoggerContext extends AbstractAgileContext {

    public static final IdGenerator idGenerator = DefaultIdGenerator.getInstance();

    private final AgileLoggerProperties properties;
    private final List<Recorder> recorders;
    private final List<ClassMatcher> classMatchers;
    private final List<MethodMatcher> methodMatchers;
    private final List<RequestIgnoreProcessor> requestIgnoreProcessors;


    private final Queue<String> parentIdQueue = new LinkedList<>();
    private final Recorder currentRecorder;
    private final RequestLoggerProcessor requestLoggerProcessor;
    private final ResponseSuccessDefineProcessor responseSuccessDefineProcessor;
    private final String contextPath;

    public AgileLoggerContext(ApplicationContext applicationContext, AgileLoggerProperties properties, List<Recorder> recorders,
                              List<ClassMatcher> classMatchers, List<MethodMatcher> methodMatchers, List<RequestIgnoreProcessor> requestIgnoreProcessors) {
        super((GenericApplicationContext) applicationContext);
        this.properties = properties;
        this.recorders = recorders;
        this.classMatchers = classMatchers;
        this.methodMatchers = methodMatchers;
        this.requestIgnoreProcessors = requestIgnoreProcessors;
        this.currentRecorder = initCurrentRecorder(properties);
        this.requestLoggerProcessor = getBeanOrDefault(RequestLoggerProcessor.class, new DefaultRequestLoggerProcessor());
        this.responseSuccessDefineProcessor = generateResponseSuccessDefineProcessor();
        this.contextPath = applicationContext.getEnvironment().getProperty("server.servlet.context-path");
    }

    /**
     * Get an existing ResponseSuccessDefineProcessor or create a default
     *
     * @return {@link ResponseSuccessDefineProcessor}
     */
    private ResponseSuccessDefineProcessor generateResponseSuccessDefineProcessor() {
        ResponseSuccessDefineProcessor defaultResponseSuccessDefineProcessor = getBeanOrDefault(ResponseSuccessDefineProcessor.class, new DefaultResponseSuccessDefineProcessorProcessor());
        AgileLoggerProperties.Logger.ResponseSuccessDefine userResponseSuccessDefine = this.properties.getLogger().getResponseSuccessDefine();
        if (userResponseSuccessDefine != null) {
            defaultResponseSuccessDefineProcessor.setResponseSuccessDefine(userResponseSuccessDefine);
        }
        return defaultResponseSuccessDefineProcessor;
    }

    /** 初始化当前配置的日志记录器 */
    private Recorder initCurrentRecorder(AgileLoggerProperties properties) {
        LogMode logMode = properties.getLogger().getLogMode();
        Recorder recorder = null;
        for (Recorder r : recorders) {
            if (r.support(logMode)) {
                recorder = r;
            }
        }
        if (recorder == null) {
            recorder = new LogRecorder(properties);
        }
        return recorder;
    }
}
