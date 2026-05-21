package org.cryptotrader.data;

import org.cryptotrader.data.library.component.CurrencyJsonGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@EntityScan(basePackages = {
        "org.cryptotrader.api.library.entity",
        "org.cryptotrader.data.library.entity"
})
@ComponentScan(basePackages = {
        "org.cryptotrader.data",
        "org.cryptotrader.api.library.component",
        "org.cryptotrader.api.library",
        "org.cryptotrader.data.library",
        "org.cryptotrader.data.library.services",
        "org.cryptotrader.data.library.component",
        "org.cryptotrader.universal.library.events"
})
@EnableJpaRepositories(basePackages = {
        "org.cryptotrader.api.library.repository",
        "org.cryptotrader.data.library.repository"
})
public class CryptoTraderDataApplication {
    public static void main(String[] args) {
        boolean loadCurrencies = shouldLoadCurrencies();
        if (loadCurrencies) {
            CurrencyJsonGenerator.standalone().generateAndSave();
        }
        enableCurrencyHarvesting();
        SpringApplication.run(CryptoTraderDataApplication.class, args);
    }

    private static boolean shouldLoadCurrencies() {
        String loadCurrenciesEnv = System.getenv().getOrDefault("CRYPTO_TRADER_LOAD_CURRENCIES", "true");
        String loadCurrenciesSetting = System.getProperty("cryptotrader.load.currency", loadCurrenciesEnv);
        boolean loadCurrencies = Boolean.parseBoolean(loadCurrenciesSetting);
        return loadCurrencies;
    }

    private static void enableCurrencyHarvesting() {
        System.setProperty("cryptotrader.harvest.currency", "true");
    }
}