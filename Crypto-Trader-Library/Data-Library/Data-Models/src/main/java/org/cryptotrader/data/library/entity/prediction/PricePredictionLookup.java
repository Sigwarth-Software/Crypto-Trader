package org.cryptotrader.data.library.entity.prediction;

import org.jetbrains.annotations.NotNull;

/** Operations used to find price prediction entities. */
public interface PricePredictionLookup {
    PricePrediction getById(@NotNull final Long id);
}
