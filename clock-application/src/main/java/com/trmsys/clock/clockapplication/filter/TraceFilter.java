package com.trmsys.clock.clockapplication.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter extends GenericFilterBean {

    public static final String FF_TRACE_ID = "ff-trace-id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ffTraceId = httpRequest.getHeader(FF_TRACE_ID);
        if (ffTraceId != null) {
            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(FF_TRACE_ID, ffTraceId)) {
                chain.doFilter(httpRequest, response);
            }
        } else {
            chain.doFilter(httpRequest, response);
        }
    }
}
