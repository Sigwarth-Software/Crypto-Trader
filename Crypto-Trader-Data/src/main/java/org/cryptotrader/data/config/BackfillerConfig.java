package org.cryptotrader.data.config;

import org.cryptotrader.data.library.services.harvest.CurrencyHarvesterService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BackfillerConfig {
    //==============================-Beans-===================================

    //------------------------Snapshots-Cli-Runner----------------------------
    @Bean
    CommandLineRunner snapshotsCliRunner(final CurrencyHarvesterService currencyService,
                                         final ApplicationArguments args) {
        return commandLineArgs -> {
            if (args.containsOption("buildSnapshots")) {
                final boolean fullRefresh = args.containsOption("fullRefresh");
                currencyService.buildMarketSnapshots(fullRefresh);
            }
        };
    }
}