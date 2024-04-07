package io.github.thebesteric.framework.agile.plugins.logger.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * MetricRequestLog
 *
 * @author Eric Joe
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MetricsRequestLog extends RequestLog {

    private Metrics metrics;

    public MetricsRequestLog(RequestLog requestLog, Metrics metrics) {
        BeanUtils.copyProperties(requestLog, this);
        this.metrics = metrics;
    }

    @Getter
    @Setter
    public static class Metrics {
        private long totalRequest = 0L;
        private long avgResponseTime = 0L;
        private long minResponseTime = 0L;
        private long maxResponseTime = 0L;
        private String maxResponseTrackId;
        private String minResponseTrackId;
        @JsonIgnore
        private long totalResponseTime = 0L;

        public synchronized void calc(RequestLog requestLog) {
            Long duration = requestLog.getDuration();
            totalRequest++;
            totalResponseTime += duration;
            avgResponseTime = totalResponseTime / totalRequest;
            if (duration > maxResponseTime) {
                maxResponseTime = duration;
                maxResponseTrackId = requestLog.getTrackId();
            }
            if (duration < minResponseTime || minResponseTime == 0L) {
                minResponseTime = duration;
                minResponseTrackId = requestLog.getTrackId();
            }
        }
    }

}
