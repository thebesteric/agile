package io.github.thebesteric.framework.agile.plugins.logger.processor.recorder;

import io.github.thebesteric.framework.agile.commons.exception.InvalidParamsException;
import io.github.thebesteric.framework.agile.commons.util.CurlUtils;
import io.github.thebesteric.framework.agile.commons.util.IOUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.commons.util.UrlUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.domain.InvokeLog;
import io.github.thebesteric.framework.agile.plugins.logger.domain.RequestLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AbstractThreadPoolRecorder
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:30:23
 */
@Slf4j
public abstract class AbstractThreadPoolRecorder implements Recorder {

    private final AgileLoggerProperties properties;
    private ExecutorService recorderThreadPool;

    protected AbstractThreadPoolRecorder(AgileLoggerProperties properties) {
        this.properties = properties;
        if (this.properties.getAsync().isEnable()) {
            this.recorderThreadPool = generateExecutorService();
        }
    }

    @Override
    public void process(InvokeLog invokeLog) {
        // Record CURL
        if (this.properties.getLogger().isCurlEnable()) {
            setCurl(invokeLog);
        }
        if (this.recorderThreadPool != null) {
            this.recorderThreadPool.execute(() -> doProcess(invokeLog));
        } else {
            doProcess(invokeLog);
        }
    }

    protected abstract void doProcess(InvokeLog invokeLog);

    /** 初始化日志记录器线程池 */
    private ExecutorService generateExecutorService() {
        AgileLoggerProperties.Async async = this.properties.getAsync();
        if (async.isEnable()) {
            AgileLoggerProperties.Async.AsyncParams asyncParams = async.getAsyncParams();
            int corePoolSize = asyncParams.getCorePoolSize();
            int maximumPoolSize = asyncParams.getMaximumPoolSize();
            if (corePoolSize > maximumPoolSize) {
                throw new InvalidParamsException("CorePoolSize cannot be less than maximumPoolSize: %d < %d", corePoolSize, maximumPoolSize);
            }
            return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, asyncParams.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(asyncParams.getQueueSize()),
                    new BasicThreadFactory.Builder().namingPattern(asyncParams.getThreadNamePrefix() + "-%d").daemon(true).build(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }
        return null;
    }

    /**
     * Set CURL when log is RequestLog
     */
    private void setCurl(InvokeLog invokeLog) {
        if (invokeLog instanceof RequestLog requestLog) {
            String curl = CurlUtils.builder()
                    .url(requestLog.getUrl())
                    .method(requestLog.getMethod())
                    .contentType(requestLog.getContentType())
                    .urlQuery(UrlUtils.queryStringToMap(requestLog.getQuery()))
                    .fromParams(requestLog.getParams())
                    .headers(requestLog.getHeaders())
                    .body(IOUtils.toByteArray(requestLog.getBody())).curl();
            requestLog.setCurl(curl);
            LoggerPrinter.trace(log, curl);
        }
    }

}
