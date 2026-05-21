package org.cryptotrader.api;

import org.cryptotrader.universal.library.component.SystemScripts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@ConfigurationPropertiesScan
@EntityScan(basePackages = {
        "org.cryptotrader.api.library.entity",
        "org.cryptotrader.data.library.entity",
        "org.cryptotrader.security.library.entity"
})
@ComponentScan(basePackages = {
        "org.cryptotrader.api",
        "org.cryptotrader.api.library.component",
        "org.cryptotrader.api.library.config",
        "org.cryptotrader.api.library.services",
        "org.cryptotrader.api.library.infrastructure",
        "org.cryptotrader.data.library.services",
        "org.cryptotrader.data.library.services.harvest",
        "org.cryptotrader.data.library.component",
        "org.cryptotrader.security.library.service",
        "org.cryptotrader.console.library.component",
        "org.cryptotrader.universal.library.component",
        "org.cryptotrader.universal.library.events",
        "org.cryptotrader.logging.library.events.publisher",
        "org.cryptotrader.simulator.library.events",
        "org.cryptotrader.simulator.library.services",
        "org.cryptotrader.simulator.library.config",
})
public class CryptoTraderApiApplication {
    public static void main(String[] args) {
        SystemScripts.blockCurrencyLoading();
        SystemScripts.blockCurrencyHarvesting();
        SpringApplication.run(CryptoTraderApiApplication.class, args);
    }
}
