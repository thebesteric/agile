package io.github.thebesteric.framework.agile.core.func;

/**
 * 成功或失败的执行器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 18:51:29
 */
public interface SuccessFailureExecutor<S, F, E> {
    void success(S result);
    void failure(F result);
    void exception(E result);
}
