package io.github.thebesteric.framework.agile.commons.util;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 执行时间计算工具类
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-07-25 15:23
 */
public class DurationWatcher extends AbstractUtils {

    private static final ThreadLocal<Map<String, Duration>> DURATION_THREAD_LOCAL = ThreadLocal.withInitial(HashMap::new);

    public static synchronized String start() {
        return start(UUID.randomUUID().toString());
    }

    public static synchronized String start(String tag) {
        Map<String, Duration> durations = DURATION_THREAD_LOCAL.get();
        Duration duration = Duration.builder().tag(tag).startTime(System.currentTimeMillis()).build();
        durations.put(tag, duration);
        DURATION_THREAD_LOCAL.set(durations);
        return tag;
    }

    public static Duration stop(String tag) {
        Map<String, Duration> durations = DURATION_THREAD_LOCAL.get();
        Duration duration = durations.get(tag);
        duration.setEndTime(System.currentTimeMillis());
        remove(tag);
        return duration;
    }

    public static void remove(String tag) {
        Map<String, Duration> durations = DURATION_THREAD_LOCAL.get();
        durations.remove(tag);
    }

    public static Duration get(String tag) {
        Map<String, Duration> durations = DURATION_THREAD_LOCAL.get();
        return durations.get(tag);
    }

    public static void clear() {
        DURATION_THREAD_LOCAL.remove();
    }

    @Data
    public static class Duration {

        private Thread thread;
        private String tag;
        private long startTime;
        private long endTime;
        private long durationTime;

        private Duration() {
            super();
        }

        public static Builder builder() {
            return new Builder(new Duration());
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
            this.durationTime = this.endTime - this.startTime;
        }

        public Date getStartTimeToDate() {
            return new Date(this.startTime);
        }

        public static class Builder {

            private final Duration duration;

            public Builder(Duration duration) {
                this.duration = duration;
                this.duration.thread = Thread.currentThread();
            }

            public Duration build() {
                return this.duration;
            }

            public Builder tag(String tag) {
                this.duration.tag = tag;
                return this;
            }

            public Builder startTime(long startTime) {
                this.duration.startTime = startTime;
                return this;
            }

            public Builder endTime(long endTime) {
                this.duration.endTime = endTime;
                this.duration.durationTime = this.duration.endTime - this.duration.startTime;
                return this;
            }
        }
    }

}
