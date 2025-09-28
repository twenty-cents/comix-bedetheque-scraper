package com.comix.scrapers.bedetheque.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ApiLanguageHelper {

    @Value("#{'${application.lang.accepted_languages}'.split(',')}")
    private List<String> acceptedLanguages;

    @Value("${application.lang.defaultLanguage}")
    private String defaultLanguage;

    /**
     * Validate the selected API language
     * @param language the API language to apply
     * @return the selected API language or the defaylt one.
     */
    public String validateAndApplyApiLanguage(String language) {
        if(language == null) {
            language = "";
        }
        if(isValidLanguage(language)) {
            return language;
        } else {
            log.warn("API Language {} not supported, applying API default language : {}", language, defaultLanguage);
            return defaultLanguage;
        }
    }

    /**
     * Check if the selected API language is supported by the API.
     * @param language the selected API language
     * @return true if the selected API language is supported by the API.
     */
    public boolean isValidLanguage(String language) {
        for(String lang : acceptedLanguages) {
            if(language != null && language.equals(lang)) {
                return true;
            }
        }
        return false;
    }
}
