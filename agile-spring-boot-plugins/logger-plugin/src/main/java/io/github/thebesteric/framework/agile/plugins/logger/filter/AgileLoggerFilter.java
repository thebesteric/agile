package io.github.thebesteric.framework.agile.plugins.logger.filter;

import io.github.thebesteric.framework.agile.commons.util.DurationWatcher;
import io.github.thebesteric.framework.agile.commons.util.TransactionUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.domain.IgnoredMethod;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerRequestWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerResponseWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.processor.ignore.RequestIgnoreProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.RequestLoggerProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.response.ResponseSuccessDefineProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.LocalLogRecorder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * AgileLoggerRequestLogFilter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-04-07 21:37:15
 */
public class AgileLoggerFilter extends AbstractAgileLoggerFilter {

    public AgileLoggerFilter(AgileLoggerContext agileLoggerContext) {
        super(agileLoggerContext);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // Get URI
        String uri = getRelativeRequestURI(request);

        // Fetch URI mapping Method
        Method method = URL_MAPPING.get(uri);
        if (method == null) {
            filterChain.doFilter(request, response);
            return;
        }
        Class<?> targetClass = method.getDeclaringClass();

        // 该方法是否需要忽略
        Set<IgnoredMethod> ignoredMethods = this.findIgnoredMethods(targetClass);
        boolean ignoredMethodMatches = this.ignoreMethodMatchers(method, ignoredMethods, agileLoggerContext.getMethodMatchers());
        if (ignoredMethodMatches) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestLogId = UUID.randomUUID().toString();
        agileLoggerContext.getParentIdQueue().add(requestLogId);

        // 转换 request & response
        AgileLoggerRequestWrapper requestWrapper = new AgileLoggerRequestWrapper((HttpServletRequest) request);
        AgileLoggerResponseWrapper responseWrapper = new AgileLoggerResponseWrapper((HttpServletResponse) response);

        // 初始化相关属性
        initProperties(requestWrapper);

        // 本地日志构建器
        LocalLogRecorder.LocalLogRecord.Builder localLogRecordBuilder = LocalLogRecorder.LocalLogRecord.builder();

        // 开始计时
        String durationTag = DurationWatcher.start();
        Exception exception = null;
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            exception = ex;
            // 本地日志增加异常信息
            localLogRecordBuilder.exception(ex);
            // Program exceptions
            responseWrapper.setException(ex);
            responseWrapper.setBuffer(ex.getMessage());
            throw ex;
        } finally {
            DurationWatcher.Duration duration = DurationWatcher.stop(durationTag);

            // Create RequestLog
            RequestLoggerProcessor requestLoggerProcessor = agileLoggerContext.getRequestLoggerProcessor();
            RequestLog requestLog = requestLoggerProcessor.processor(requestLogId, method, requestWrapper, responseWrapper, duration);

            // Process non-program exceptions, For example: code != 200
            ResponseSuccessDefineProcessor responseSuccessDefineProcessor = agileLoggerContext.getResponseSuccessDefineProcessor();
            String exceptionInfo = responseSuccessDefineProcessor.processor(requestLog.getResult());
            if (exception != null) {
                requestLog.setException(exceptionInfo);
                requestLog.setLevel(LogLevel.ERROR);
            }
            requestLog.setExceptionClass(exception == null ? null : exception.getClass());

            // 处理需要忽略的请求参数
            List<RequestIgnoreProcessor> requestIgnoreProcessors = agileLoggerContext.getRequestIgnoreProcessors();
            requestIgnoreProcessors = Optional.ofNullable(requestIgnoreProcessors).orElse(new ArrayList<>());
            requestIgnoreProcessors.forEach(requestIgnoreProcessor -> {
                requestIgnoreProcessor.ignore(requestLog);
                requestIgnoreProcessor.rewrite(requestLog);
            });

            // Record request info
            // 记录日志
            Recorder currentRecorder = agileLoggerContext.getCurrentRecorder();
            currentRecorder.process(requestLog);

            // 记录本地日志
            AgileLoggerProperties properties = agileLoggerContext.getProperties();
            AgileLoggerProperties.LocalLogRecorderConfig localLogRecorderConfig = properties.getLocalLogRecorderConfig();
            // 开启状态，并且 beforeRecord 返回 true
            if (localLogRecorderConfig.isEnable() && agileLoggerContext.getLocalLogRecordPostProcessor().postProcessBeforeRecord(requestLog)) {
                localLogRecordBuilder.invokeLog(requestLog);
                LocalLogRecorder.record(localLogRecorderConfig, localLogRecordBuilder.build());
            }

            agileLoggerContext.getParentIdQueue().clear();
            DurationWatcher.clear();
            TransactionUtils.clear();

            ServletOutputStream out = response.getOutputStream();
            out.write(responseWrapper.getByteArray());
            out.flush();
        }

    }

    /** 初始化相关属性 */
    private void initProperties(HttpServletRequest request) {
        // 设置自定义的 trackId
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if (TransactionUtils.hasTrackIdInRequestHeader(headerName)) {
                TransactionUtils.set(headerValue);
                break;
            }
        }
    }

}
