package io.github.thebesteric.framework.agile.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AgileLoggerAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 14:47:25
 */
@Configuration
@ConditionalOnBean(AgileMarker.class)
@Import({AgileApplicationContextInitializer.class, AgilePluginsSelector.class})
public class AgileAutoConfiguration {

}
