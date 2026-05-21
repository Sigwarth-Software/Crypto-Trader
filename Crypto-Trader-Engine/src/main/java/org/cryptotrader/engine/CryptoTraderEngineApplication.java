package org.cryptotrader.engine;

import org.cryptotrader.health.library.model.CryptoTraderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import static org.cryptotrader.health.library.model.ServiceStatusChecker.isServiceAlive;


/**
 * Main entry point for the CryptoTrader Engine application. This class
 * initializes and configures the Spring Boot application with support for
 * asynchronous processing, scheduling, JPA repositories, and entity scanning.
 * Additionally, it provides mechanisms to enable or disable trading based on
 * external configuration through environment variables or system properties.
 *
 * @author theoliverlear - Oliver Lear Sigwarth
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@EntityScan(basePackages = {
    "org.cryptotrader.api.library.entity",
    "org.cryptotrader.data.library.entity"
})
@ComponentScan(basePackages = {
    "org.cryptotrader.api.library.component",
    "org.cryptotrader.engine",
    "org.cryptotrader.api.library",
    "org.cryptotrader.data.library",
    "org.cryptotrader.engine.library.services",
    "org.cryptotrader.universal.library.events"
})
@EnableJpaRepositories(basePackages = {
    "org.cryptotrader.api.library.repository",
    "org.cryptotrader.data.library.repository"
})
public class CryptoTraderEngineApplication {
    public static void main(String[] args) {
        boolean tradingEnabled = isTradingEnabled();
        if (!tradingEnabled) {
            disableTrading();
        }
        disableCurrencyHarvesting();
        SpringApplication.run(CryptoTraderEngineApplication.class, args);
    }

    private static void disableTrading() {
        System.setProperty("spring.task.scheduling.enabled", "false");
    }

    private static void disableCurrencyHarvesting() {
        System.setProperty("cryptotrader.harvest.currency", "false");
    }

    private static boolean isTradingEnabled() {
        String tradingEnabledEnv = System.getenv().getOrDefault("CRYPTO_TRADER_ENGINE_TRADING_ENABLED", "true");
        String tradingEnabledProperty = System.getProperty("cryptotrader.engine.trading.enabled", tradingEnabledEnv);
        boolean tradingEnabled = Boolean.parseBoolean(tradingEnabledProperty);
        return tradingEnabled;
    }

    @Component
    @Profile("!docs")
    static class EngineStartupVerifier implements ApplicationRunner {
        private static final int MAX_RETRIES = 10;
        private static final long RETRY_DELAY_MS = 10_000;

        @Override
        public void run(ApplicationArguments args) {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                if (isServiceAlive(CryptoTraderService.DATA)) {
                    return;
                }
                System.out.printf("Crypto-Trader-Data service is not available. Attempt %d of %d. Retrying in %d seconds...%n", attempt, MAX_RETRIES, RETRY_DELAY_MS / 1000);
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(
                            "Interrupted while waiting for Crypto-Trader-Data service.", exception);
                    }
                }
            }
            throw new IllegalStateException(
                "Crypto-Trader-Data service is not available after " + MAX_RETRIES +
                    " attempts. Please start it before launching Crypto-Trader-Engine.");
        }
    }
}
