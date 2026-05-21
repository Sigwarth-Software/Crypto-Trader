package org.cryptotrader.data.library.services.harvest;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.data.library.component.NewsSentimentHarvesterClient;
import org.cryptotrader.data.library.entity.news.NewsSentiment;
import org.cryptotrader.data.library.repository.NewsSentimentRepository;
import org.cryptotrader.data.library.services.NewsSentimentService;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NewsSentimentHarvesterService {
    private final NewsSentimentRepository newsSentimentRepository;
    private final NewsSentimentHarvesterClient sentimentHarvesterClient;
    private final NewsSentimentService newsSentimentService;
    public NewsSentimentHarvesterService(NewsSentimentRepository newsSentimentRepository,
                                         NewsSentimentHarvesterClient sentimentHarvesterClient,
                                         NewsSentimentService newsSentimentService) {
        this.newsSentimentRepository = newsSentimentRepository;
        this.sentimentHarvesterClient = sentimentHarvesterClient;
        this.newsSentimentService = newsSentimentService;
    }
    // TODO: Use event listeners to trigger these methods.

    //    @Async
//    @Scheduled(fixedRate = 3600000)
    @TimeTracked(expectedMillis = 10000, shouldPersist = true)
    public void triggerDailySentimentHarvest() {
        log.info("Triggering hourly news sentiment harvest...");
        this.sentimentHarvesterClient.triggerHarvest();
    }

    //    @Async
//    @Scheduled(fixedRate = 2592000000L)
    @TimeTracked(expectedMillis = 60000, shouldPersist = true)
    public void backFillDailySentimentHarvest() {
        log.info("Back-filling daily news sentiment harvest...");
        this.sentimentHarvesterClient.backFillDaily();
    }

    //    @Async
//    @Scheduled(fixedRate = 604800000L)
    @TimeTracked(expectedMillis = 300000, shouldPersist = true)
    public void backFillWeeklySentimentHarvest() {
        log.info("Back-filling weekly news sentiment harvest...");
        this.sentimentHarvesterClient.backFillWeekly();
    }

    public boolean shouldHarvest() {
        LocalDateTime now = LocalDateTime.now();
        NewsSentiment lastInserted = this.newsSentimentRepository.getLastInserted();
        if (lastInserted == null) {
            return true;
        }
        LocalDateTime lastHarvest = lastInserted.getLastUpdated();
        return now.isAfter(lastHarvest.plusHours(1));
    }

    private void handleOutdatedHarvest() {
        if (this.shouldHarvest()) {
            log.info("Harvest sentiments are out of date. Triggering a new harvest.");
            this.triggerDailySentimentHarvest();
        }
    }
}
