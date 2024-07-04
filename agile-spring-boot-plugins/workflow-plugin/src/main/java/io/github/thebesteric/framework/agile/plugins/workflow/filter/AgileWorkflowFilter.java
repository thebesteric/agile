package io.github.thebesteric.framework.agile.plugins.workflow.filter;

import io.github.thebesteric.framework.agile.plugins.workflow.config.AgileWorkflowContext;
import jakarta.servlet.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * AgileWorkflowFilter
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-06-21 14:49:57
 */
@RequiredArgsConstructor
public class AgileWorkflowFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(request, response);
        AgileWorkflowContext.removeCurrentUser();
    }
}
