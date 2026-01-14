package io.github.thebesteric.framework.agile.wechat.third.platform.config;

import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.core.AgileConstants;
import io.github.thebesteric.framework.agile.core.config.AbstractAgileInitialization;
import io.github.thebesteric.framework.agile.wechat.third.platform.WechatMiniHelper;
import io.github.thebesteric.framework.agile.wechat.third.platform.WechatThirdPlatformHelper;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.mini.WechatMiniProperties;
import io.github.thebesteric.framework.agile.wechat.third.platform.config.third.WechatThirdPlatformProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgileWechatOpenAutoConfiguration
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-05 17:48:47
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({WechatMiniProperties.class, WechatThirdPlatformProperties.class})
@RequiredArgsConstructor
public class AgileWechatOpenAutoConfiguration extends AbstractAgileInitialization {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private final WechatMiniProperties wechatMiniProperties;
    private final WechatThirdPlatformProperties wechatThirdPlatformProperties;

    @Override
    public void start() {
        if (wechatMiniProperties.isEnable()) {
            loggerPrinter.info("Wechat-mini-plugin is running");
        }
        if (wechatThirdPlatformProperties.isEnable()) {
            loggerPrinter.info("Wechat-third-platform-plugin is running");
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".wechat.mini", name = "enable", havingValue = "true")
    public WechatMiniHelper wechatMiniHelper(WechatMiniProperties wechatMiniProperties) {
        return new WechatMiniHelper(wechatMiniProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AgileConstants.PROPERTIES_PREFIX + ".wechat.third", name = "enable", havingValue = "true")
    public WechatThirdPlatformHelper wechatThirdPlatformHelper(WechatThirdPlatformProperties wechatThirdPlatformProperties) {
        return new WechatThirdPlatformHelper(wechatThirdPlatformProperties);
    }
}
