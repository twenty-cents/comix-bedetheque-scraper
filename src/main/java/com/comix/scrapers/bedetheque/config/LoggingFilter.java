package com.comix.scrapers.bedetheque.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class LoggingFilter implements Filter {

    private static final String TRACEID_HEADER = "X-B3-TraceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String traceId;
        log.info("Incoming request: {} {}", ((HttpServletRequest) request).getMethod(), ((HttpServletRequest) request).getRequestURI());
        try {
            MDC.put("userIP", this.getClientIp((HttpServletRequest) request));
            MDC.put("user-agent", this.getUserAgent((HttpServletRequest) request));
            traceId = this.getTraceId((HttpServletRequest) request);
            MDC.put("traceId", traceId);

            InetAddress var100001 = InetAddress.getLocalHost(); // NOSONAR
            MDC.put("hostname", var100001.getHostName());
            MDC.put("requestURI", ((HttpServletRequest) request).getRequestURI());
            MDC.put("method", ((HttpServletRequest) request).getMethod());

            TraceableServletRequestWrapper servletRequestWrapper = new TraceableServletRequestWrapper((HttpServletRequest) request); //NOSONAR
            servletRequestWrapper.addHeader(TRACEID_HEADER, traceId);

            this.setTraceId((HttpServletResponse) response, traceId);
            chain.doFilter(servletRequestWrapper, response);

        } finally {
            // Tear down MDC data
            // ( Important! Cleans up the ThreadLocal data again )
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = null;
        if(request != null) {
            remoteAddr = request.getHeader("X-FORWARD-FOR");
            if(remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return (remoteAddr == null) ? "" : remoteAddr;
    }

    private String getUserAgent(HttpServletRequest request) {
        String userAgent = null;
        if(request != null) {
            userAgent = request.getHeader("user-agent");
        }
        return (userAgent == null) ? "" : userAgent;
    }

    private String getTraceId(HttpServletRequest request) {
        String traceId = null;
        if(request != null) {
            traceId = request.getHeader(TRACEID_HEADER);
            if(traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString();
            }
        }
        return (traceId == null) ? UUID.randomUUID().toString() : traceId;
    }

    private void setTraceId(HttpServletResponse response, String traceId) {
        response.setHeader(TRACEID_HEADER, traceId); // NOSONAR
    }
}
