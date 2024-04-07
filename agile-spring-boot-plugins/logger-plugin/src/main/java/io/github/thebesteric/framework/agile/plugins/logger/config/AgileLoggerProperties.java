package io.github.thebesteric.framework.agile.plugins.logger.config;

import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.plugins.logger.constant.LogMode;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.List;

/**
 * AgileLoggerProperties
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 14:04:42
 */
@Getter
@Setter
@ConfigurationProperties(prefix = AgileConstants.PROPERTIES_PREFIX)
public class AgileLoggerProperties {

    private boolean enable = true;
    private String basePackage;
    @NestedConfigurationProperty
    private Logger logger = new Logger();
    @NestedConfigurationProperty
    private Async async = new Async();

    @Getter
    @Setter
    public static class Logger {
        private LogMode logMode = LogMode.LOG;
        private String uriPrefix;
        private boolean curlEnable = false;
        @NestedConfigurationProperty
        private ResponseSuccessDefine responseSuccessDefine;

        @Getter
        @Setter
        public static class ResponseSuccessDefine {
            private List<CodeField> codeFields;
            private List<String> messageFields;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class CodeField {
                private String name;
                private Object value;
            }
        }
    }

    @Getter
    @Setter
    public static class Async {
        private boolean enable = true;
        @NestedConfigurationProperty
        private AsyncParams asyncParams = new AsyncParams();

        @Getter
        @Setter
        public static class AsyncParams {
            private int corePoolSize = Runtime.getRuntime().availableProcessors();
            private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
            private Duration keepAliveTime = Duration.ofSeconds(60);
            private int queueSize = 1024;
            private String threadNamePrefix = "agile-logger-thread-pool";

            @Override
            public String toString() {
                return "[" +
                        "corePoolSize=" + corePoolSize + ", " +
                        "maximumPoolSize=" + maximumPoolSize + ", " +
                        "queueSize=" + queueSize + ", " +
                        "keepAliveTime=" + keepAliveTime.getSeconds() + "s" +
                        "]";
            }
        }
    }

}
