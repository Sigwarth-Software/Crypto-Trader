package org.cryptotrader.data.library.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.currency.SupportedCurrencies;
import org.cryptotrader.data.library.model.http.ApiDataRetriever;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class CurrencyDataRetriever extends ApiDataRetriever {
    private static final String API_URL = "https://api.coinbase.com/v2/exchange-rates?currency=USD";
    public CurrencyDataRetriever() {
        super(API_URL);
    }
    @TimeTracked(expectedMillis = 125, shouldPersist = true)
    public @NotNull Map<String ,Currency> getUpdatedCurrencies() {
        final Map<String, Double> currencyMap = this.getCurrencyMap();
        final Map<String, Currency> updatedCurrencyMap = new HashMap<>();
        for (final Map.Entry<String, Double> entry : currencyMap.entrySet()) {
            final String currencyCode = entry.getKey();
            final double value = entry.getValue();
            final Currency currency = Currency.builder()
                    .currencyCode(currencyCode)
                    .value(value)
                    .build();
            updatedCurrencyMap.put(currencyCode, currency);
        }
        final List<String> currencyMapKeys = new ArrayList<>(updatedCurrencyMap.keySet());
        final List<String> supportedCurrenciesKeys = SupportedCurrencies.SUPPORTED_CURRENCIES.stream().map(Currency::getCurrencyCode).toList();
        if (supportedCurrenciesKeys.isEmpty()) {
            return updatedCurrencyMap;
        }
        final Map<String, Currency> filteredCurrencyMap = new HashMap<>();
        for (final String currencyCode : supportedCurrenciesKeys) {
            if (currencyMapKeys.contains(currencyCode)) {
                filteredCurrencyMap.put(currencyCode, updatedCurrencyMap.get(currencyCode));
            }
        }
        return filteredCurrencyMap;
    }
    public @NotNull Map<String, Double> getCurrencyMap() {
        final Map<String, Double> currencyMap = new HashMap<>();
        this.fetchResponse();
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final JsonNode rootNode = mapper.readTree(this.getResponse());
            final JsonNode ratesNode = rootNode.path("data").path("rates");
            final boolean validNode = ratesNode != null && ratesNode.isObject();
            if (validNode) {
                final Iterator<String> fieldNames = ratesNode.fieldNames();
                while (fieldNames.hasNext()) {
                    final String code = fieldNames.next();
                    final double value = 1.0 / ratesNode.get(code).asDouble();
                    currencyMap.put(code, value);
                }
            } else {
                System.err.println("Error: Invalid data format received from API.");
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Failed to parse JSON response", exception);
        }
        return currencyMap;
    }
}
