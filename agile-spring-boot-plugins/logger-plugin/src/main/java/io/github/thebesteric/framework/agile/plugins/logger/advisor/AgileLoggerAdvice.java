package io.github.thebesteric.framework.agile.plugins.logger.advisor;

import io.github.thebesteric.framework.agile.commons.util.DurationWatcher;
import io.github.thebesteric.framework.agile.commons.util.ExceptionUtils;
import io.github.thebesteric.framework.agile.commons.util.TransactionUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerContext;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogLevel;
import io.github.thebesteric.framework.agile.plugins.logger.domain.AgileLoggerHelper;
import io.github.thebesteric.framework.agile.plugins.logger.domain.ExecuteInfo;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.processor.recorder.Recorder;
import io.github.thebesteric.framework.agile.plugins.logger.recorder.LocalLogRecorder;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Queue;

/**
 * AgileMethodAdvice
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-07 15:14:05
 */
@RequiredArgsConstructor
public class AgileLoggerAdvice implements MethodInterceptor {

    private final AgileLoggerContext agileLoggerContext;

    @Override
    public Object invoke(@NonNull MethodInvocation invocation) throws Throwable {
        AgileLoggerProperties properties = agileLoggerContext.getProperties();
        if (!properties.isEnable()) {
            // 调用目标方法
            return invocation.proceed();
        }

        // 开始计时
        String durationTag = DurationWatcher.start();
        InvokeLog.Builder invokeLogBuilder = InvokeLog.builder();

        // 设置 parentId
        Queue<String> parentIdQueue = agileLoggerContext.getParentIdQueue();
        invokeLogBuilder.parentId(parentIdQueue.poll());
        parentIdQueue.add(invokeLogBuilder.id());

        // 本地日志构建器
        LocalLogRecorder.LocalLogRecord.Builder localLogRecordBuilder = LocalLogRecorder.LocalLogRecord.builder();

        Object result;
        try {
            // 调用目标方法
            result = invocation.proceed();
            invokeLogBuilder.result(result);
        } catch (Throwable throwable) {
            // 本地日志增加异常信息
            localLogRecordBuilder.exception(throwable);
            // 异常处理
            invokeLogBuilder.exception(ExceptionUtils.getSimpleMessage(throwable));
            invokeLogBuilder.level(LogLevel.ERROR);
            throw throwable;
        } finally {
            // 结束计时
            DurationWatcher.Duration duration = DurationWatcher.stop(durationTag);
            // 设置日志相关参数
            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();
            AgileLoggerHelper agileLoggerHelper = new AgileLoggerHelper(method);
            ExecuteInfo executeInfo = new ExecuteInfo(method, args);
            executeInfo.setDuration(duration.getDurationTime());
            invokeLogBuilder.executeInfo(executeInfo);
            invokeLogBuilder.trackId(TransactionUtils.get());
            invokeLogBuilder.tag(agileLoggerHelper.getTag());
            // 生成日志
            InvokeLog invokeLog = invokeLogBuilder.build();
            // 记录日志
            Recorder currentRecorder = agileLoggerContext.getCurrentRecorder();
            currentRecorder.process(invokeLog);
            // 记录本地日志
            localLogRecordBuilder.invokeLog(invokeLog);
            AgileLoggerProperties.LocalLogRecorderConfig localLogRecorderConfig = properties.getLocalLogRecorderConfig();
            LocalLogRecorder.record(localLogRecorderConfig, localLogRecordBuilder.build());

        }
        return result;
    }
}
