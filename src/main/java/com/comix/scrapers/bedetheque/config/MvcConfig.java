package com.comix.scrapers.bedetheque.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MvcConfig.class);

    @Value("${application.downloads.localcache.basepath}")
    private String sharedStoragePath;

    @Value("${application.downloads.localcache.mediasUrlPattern}")
    private String mediasUrlPattern;


    /**
     * Add a local storage for static files under /medias/**
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Constructs the absolute path to the media directory on the filesystem.
        // The "file:" prefix is crucial to tell Spring to look outside the classpath.
        // Example final value: "file:/mnt/shared/bubbles-manager/dev/medias/bedetheque/"
        String mediasLocation = "file:" + sharedStoragePath + "/";

        LOGGER.info("Static resource manager configuration for path: {}", mediasLocation);

        registry
                // 1. The URL pattern: any request starting with /medias/** will be handled by this handler.
                .addResourceHandler(mediasUrlPattern)
                // 2. The physical location: Spring will look for matching files at this location.
                .addResourceLocations(mediasLocation);
    }

    @Bean
    public LocaleResolver localeResolver() {
        var slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        var lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
