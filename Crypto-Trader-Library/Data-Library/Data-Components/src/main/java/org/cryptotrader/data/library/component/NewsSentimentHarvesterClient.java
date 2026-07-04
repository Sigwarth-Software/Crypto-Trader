package org.cryptotrader.data.library.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.cryptotrader.data.library.communication.request.NewsSentimentHarvestRequest;
import org.cryptotrader.data.library.communication.request.NewsSentimentTargetedHarvestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

@Component
@Slf4j
public class NewsSentimentHarvesterClient {
    private static final String DAILY_HARVEST_PATH = "/api/news/sentiment/harvest";
    private static final String TARGETED_HARVEST_PATH = "/api/news/sentiment/harvest/targeted/by-date";
    private static final LocalDate START_DATE = LocalDate.of(2025, Month.MARCH, 15);
    private static final int MAX_ARTICLES = 100;
    private static final NewsSentimentHarvestRequest DEFAULT_REQUEST = new NewsSentimentHarvestRequest(MAX_ARTICLES, 1, 1, true);
    private final HttpPost httpPost;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final String analysisBaseUrl;

    @Autowired
    public NewsSentimentHarvesterClient(HttpPost httpPost,
                                        ObjectMapper objectMapper,
                                        CloseableHttpClient httpClient,
                                        @Value("${cryptotrader.analysis.base-url:https://localhost:8000}") String analysisBaseUrl) {
        this.httpPost = httpPost;
        this.initHeaders();
        this.analysisBaseUrl = normalizeBaseUrl(analysisBaseUrl);
        this.httpPost.setURI(URI.create(this.getAnalysisUrl(DAILY_HARVEST_PATH)));
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    private void initHeaders() {
        this.httpPost.setHeader("Content-Type", "application/json");
        this.httpPost.setHeader("Accept", "application/json");
        this.httpPost.setHeader("Accept-Charset", "UTF-8");
    }

    public void triggerHarvest() {
        this.triggerHarvest(DEFAULT_REQUEST);
    }

    public <T> String requestToJson(T request) {
        try {
            return this.objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            log.error("Error converting request to JSON.", ex);
            return null;
        }
    }

    public void triggerHarvest(NewsSentimentHarvestRequest request) {
        this.httpPost.setURI(URI.create(this.getAnalysisUrl(DAILY_HARVEST_PATH)));
        log.info("Sending harvest request...");
        String json = this.requestToJson(request);
        try {
            this.httpPost.setEntity(new StringEntity(json));
            CloseableHttpResponse response = this.httpClient.execute(this.httpPost);
            log.info("Harvest request sent. Response status: {}", response.getStatusLine().getStatusCode());
        } catch (IOException exception) {
            log.error("Failed to set request entity.", exception);
        }
    }

    public void triggerHarvest(NewsSentimentTargetedHarvestRequest request) {
        this.httpPost.setURI(URI.create(this.getAnalysisUrl(TARGETED_HARVEST_PATH)));
        log.info("Sending targeted harvest request...");
        String json = this.requestToJson(request);
        try {
            this.httpPost.setEntity(new StringEntity(json));
            CloseableHttpResponse response = this.httpClient.execute(this.httpPost);
            log.info("Targeted harvest request sent. Response status: {}", response.getStatusLine().getStatusCode());
        } catch (IOException exception) {
            log.error("Failed to set request entity.", exception);
        }
    }

    public static NewsSentimentTargetedHarvestRequest getTargetedHarvestRequest(LocalDate startDate,
                                                                                LocalDate endDate) {
        return new NewsSentimentTargetedHarvestRequest(100, startDate, endDate, true);
    }

    public void triggerTargetedHarvest(LocalDate startDate, LocalDate endDate) {
        NewsSentimentTargetedHarvestRequest request = getTargetedHarvestRequest(startDate, endDate);
        this.triggerHarvest(request);
    }

    public void backFillMonthly() {
        LocalDate today = LocalDate.now();
        LocalDate startMonth = today;
        LocalDate endMonth = today.minusMonths(1);
        while (endMonth.isAfter(START_DATE)) {
            this.triggerTargetedHarvest(startMonth, endMonth);
            startMonth = startMonth.minusMonths(1);
            endMonth = endMonth.minusMonths(1);
        }
    }

    public void backFillWeekly() {
        LocalDate today = LocalDate.now();
        LocalDate startWeek = today;
        LocalDate endWeek = today.minusWeeks(1);
        while (endWeek.isAfter(START_DATE)) {
            this.triggerTargetedHarvest(startWeek, endWeek);
            startWeek = startWeek.minusWeeks(1);
            endWeek = endWeek.minusWeeks(1);
        }
    }

    public void backFillDaily() {
        LocalDate today = LocalDate.now();
        LocalDate startDay = today;
        LocalDate endDay = today.minusDays(1);
        while (endDay.isAfter(START_DATE)) {
            this.triggerTargetedHarvest(startDay, endDay);
            startDay = startDay.minusDays(1);
            endDay = endDay.minusDays(1);
        }
    }

    private String getAnalysisUrl(String path) {
        return this.analysisBaseUrl + path;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://localhost:8000";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
