package com.comix.scrapers.bedetheque.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleHelper {

    private static final String LOCALE = "en";

    private ResourceBundleHelper() {}

    public static String getLocalizedMessage(String key, Locale locale) {
        var bundle = ResourceBundle.getBundle("i18n/messages", locale);
        return bundle.getString(key);
    }

    public static String getLocalizedMessage(String key, String locale) {
        return getLocalizedMessage(key, Locale.forLanguageTag(locale));
    }

    public static String getLocalizedMessage(String key, Object[] args, Locale locale) {
        String pattern = getLocalizedMessage(key, locale);
        if(args != null) {
            var formatter = new MessageFormat(pattern, locale);
            pattern = formatter.format(args);
        }
        return pattern;
    }

    public static String getLocalizedMessage(String key, Object[] args, String locale) {
        return getLocalizedMessage(key, args, Locale.forLanguageTag(locale));
    }

    public static String getLocalizedMessage(String key, Object[] args) {
        String locale = getSessionLocale();
        return getLocalizedMessage(key, args, Locale.forLanguageTag(locale));
    }

    private static String getSessionLocale() {
        return LOCALE;
    }
}
