package io.github.thebesteric.framework.agile.core.func;

/**
 * 成功或失败的执行器
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-08-22 18:51:29
 */
public interface SuccessFailureExecutor<S, F, E> {
    /** 成功时调用 */
    void success(S result);
    /** 失败时调用 */
    void failure(F result);
    /** 异常时调用 */
    void exception(E result);
    /** 最终调用 */
    default void complete() {}
}
