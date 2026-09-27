package org.cryptotrader.data.library.services.adapter;

import lombok.RequiredArgsConstructor;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.data.library.entity.prediction.PricePredictionLookup;
import org.cryptotrader.data.library.services.PricePredictionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PricePredictionLookupAdapter implements PricePredictionLookup {
    private final PricePredictionService pricePredictionService;

    @Override
    public PricePrediction getById(@NotNull final Long id) {
        return this.pricePredictionService.getById(id);
    }
}
