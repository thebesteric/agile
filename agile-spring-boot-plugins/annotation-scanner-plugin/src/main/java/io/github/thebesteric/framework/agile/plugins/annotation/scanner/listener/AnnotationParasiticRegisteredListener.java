package io.github.thebesteric.framework.agile.plugins.annotation.scanner.listener;

import io.github.thebesteric.framework.agile.plugins.annotation.scanner.domain.Parasitic;

/**
 * 注解宿主已注册监听器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-13 15:49:58
 */
public interface AnnotationParasiticRegisteredListener {
    /**
     * 注解类宿主已注册
     *
     * @param parasitic 注解宿主
     */
    void onClassParasiticRegistered(Parasitic parasitic);

    /**
     * 注解方法宿主已注册
     *
     * @param parasitic 注解宿主
     */
    void onMethodParasiticRegistered(Parasitic parasitic);
}
