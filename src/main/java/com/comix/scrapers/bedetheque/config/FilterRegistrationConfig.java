package com.comix.scrapers.bedetheque.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class FilterRegistrationConfig {

    @Bean
    @ConditionalOnProperty(name = "application.logging.web", havingValue = "true")
    public FilterRegistrationBean<CommonsRequestLoggingFilter> apiFreebdsFilter() {
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(requestLoggingFilter());
        registrationBean.addUrlPatterns("/api/bedetheque-scraper/*");

        return registrationBean;
    }

    private CommonsRequestLoggingFilter requestLoggingFilter() {
        var loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setMaxPayloadLength(100000);
        loggingFilter.setBeforeMessagePrefix("incoming request : [");
        loggingFilter.setAfterMessagePrefix("incoming request body : [");
        return loggingFilter;
    }
}
