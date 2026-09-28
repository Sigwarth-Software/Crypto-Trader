package org.cryptotrader.data.library.services.config;

import org.cryptotrader.data.library.entity.prediction.PricePredictionLookup;
import org.cryptotrader.data.library.entity.training.builder.TrainingSessionBuilder;
import org.cryptotrader.data.library.services.PricePredictionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class BuildersConfig {

    @Bean
    @Primary
    public @NotNull PricePredictionLookup pricePredictionLookup(final @NotNull PricePredictionService pricePredictionService) {
        return pricePredictionService::getById;
    }

    @Bean
    @Scope("prototype")
    public @NotNull TrainingSessionBuilder trainingSessionBuilder(final @NotNull PricePredictionLookup predictionLookup) {
        return new TrainingSessionBuilder(predictionLookup);
    }
}
