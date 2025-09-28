package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.util.HTML;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

public abstract class Scraper {

    public static final String JSOUP_TAG_COUNT = ".count";

    public String attr(Element element, HTML.Attribute attributeKey) {
        return (element == null) ? null : element.attr(attributeKey.toString()).trim();
    }

    public String ownText(Element element) {
        String text = (element == null) ? null : element.ownText();
        return (!StringUtils.isBlank(text)) ? text.trim() : null;
    }

    public String text(Element element) {
        String text = (element == null) ? null : element.text();
        return (!StringUtils.isBlank(text)) ? text.trim() : null;
    }
}
