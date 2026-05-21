package org.cryptotrader.data.library.services;

import org.cryptotrader.data.library.communication.request.PricePredictionRequest;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.prediction.PricePrediction;
import org.cryptotrader.data.library.repository.PricePredictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.stereotype.Service;


@Service
public class PricePredictionService {
    private final CurrencyService currencyService;
    private final PricePredictionRepository pricePredictionRepository;
    @Autowired
    public PricePredictionService(CurrencyService currencyService,
                                  PricePredictionRepository pricePredictionRepository) {
        this.currencyService = currencyService;
        this.pricePredictionRepository = pricePredictionRepository;
    }
    @TimeTracked(expectedMillis = 500, shouldPersist = true)
    public PricePrediction savePrediction(PricePredictionRequest pricePredictionRequest) {
        Currency currency = this.currencyService.getCurrencyByCurrencyCode(pricePredictionRequest.getCurrencyCode());
        PricePrediction pricePrediction = PricePrediction.builder()
                .currencyCode(pricePredictionRequest.getCurrencyCode())
                .currencyName(currency.getName())
                .predictedPrice(pricePredictionRequest.getPredictedPrice())
                .actualPrice(pricePredictionRequest.getActualPrice())
                .priceDifference(pricePredictionRequest.getPriceDifference())
                .percentDifference(pricePredictionRequest.getPercentDifference())
                .modelType(pricePredictionRequest.getModelType())
                .numRows(pricePredictionRequest.getNumRows())
                .lastUpdated(pricePredictionRequest.getLastUpdated())
                .build();
        return this.pricePredictionRepository.save(pricePrediction);
    }

    public PricePrediction getById(Long id) {
        return this.pricePredictionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Price prediction not found with id: " + id));
    }
}
