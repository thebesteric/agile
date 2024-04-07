package io.github.thebesteric.framework.agile.plugins.logger.filter;

import io.github.thebesteric.framework.agile.commons.util.DurationWatcher;
import io.github.thebesteric.framework.agile.commons.util.TransactionUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.domain.IgnoredMethod;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerRequestWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.filter.warpper.AgileLoggerResponseWrapper;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.processor.request.RequestLoggerProcessor;
import io.github.thebesteric.framework.agile.plugins.logger.processor.response.ResponseSuccessDefineProcessor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

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

        // 开始计时
        String durationTag = DurationWatcher.start();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
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
            String exception = responseSuccessDefineProcessor.processor(requestLog.getResult());
            if (exception != null) {
                requestLog.setException(exception);
                requestLog.setLevel(LogLevel.ERROR);
            }

            // Record request info
            // 记录日志
            Recorder currentRecorder = agileLoggerContext.getCurrentRecorder();
            currentRecorder.process(requestLog);

            agileLoggerContext.getParentIdQueue().clear();
            DurationWatcher.clear();
            TransactionUtils.clear();

            ServletOutputStream out = response.getOutputStream();
            out.write(responseWrapper.getByteArray());
            out.flush();
        }

    }
}
