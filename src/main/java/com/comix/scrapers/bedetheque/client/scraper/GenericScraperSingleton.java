package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
public class GenericScraperSingleton {

    private static GenericScraperSingleton instance;
    private static final String ERR_SCR_001 = "ERR-SCR-001";

    private OffsetDateTime lastLoading = OffsetDateTime.now();

    private GenericScraperSingleton() {}

    public static GenericScraperSingleton getInstance() {
        //synchronized block to remove overhead
        synchronized (GenericScraperSingleton.class) {
            if(instance==null) {
                // if instance is null, initialize
                instance = new GenericScraperSingleton();
            }
        }
        return instance;
    }

    /**
     * Load the HTML content of an url
     * @param url the url to load.
     * @return the HTML content.
     */
    public Document load(String url, long latency) {
        waitForLoading(url, latency);

        Document doc ;
        try {
             doc = Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla").get();
        } catch (IOException e) {
            throw new TechnicalException(ERR_SCR_001, e, new Object[]{url});
        }
        lastLoading = OffsetDateTime.now();
        return doc;
    }

    public Document load(String url, Map<String, String> data, long latency) {
        waitForLoading(url, latency);

        Document doc ;
        Connection.Response response;
        try {
            response = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36")
                    .referrer(url)
                    .method(Connection.Method.GET)
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("connection", "keep-alive")
                    .data(data)
                    .execute();
            doc = response.parse();
        } catch (IOException e) {
            throw new TechnicalException(ERR_SCR_001, e, new Object[]{url});
        }
        lastLoading = OffsetDateTime.now();
        return doc;
    }

    private void waitForLoading(String url, long latency) {
        OffsetDateTime nextAcceptableDate = lastLoading.plusSeconds(latency);
        if(nextAcceptableDate.isAfter(OffsetDateTime.now())) {
            try {
                Thread.sleep(latency * 1000);
            } catch (InterruptedException e) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                throw new TechnicalException(ERR_SCR_001, e, new Object[]{url});
            }
        }
    }
}
