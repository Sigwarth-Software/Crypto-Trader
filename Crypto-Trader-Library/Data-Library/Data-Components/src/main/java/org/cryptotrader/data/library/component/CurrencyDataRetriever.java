package org.cryptotrader.data.library.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.currency.SupportedCurrencies;
import org.cryptotrader.data.library.model.http.ApiDataRetriever;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class CurrencyDataRetriever extends ApiDataRetriever {
    private static final String API_URL = "https://api.coinbase.com/v2/exchange-rates?currency=USD";
    public CurrencyDataRetriever() {
        super(API_URL);
    }
    @TimeTracked(expectedMillis = 3000, shouldPersist = true)
    public Map<String ,Currency> getUpdatedCurrencies() {
        Map<String, Double> currencyMap = this.getCurrencyMap();
        Map<String, Currency> updatedCurrencyMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : currencyMap.entrySet()) {
            String currencyCode = entry.getKey();
            double value = entry.getValue();
            Currency currency = Currency.builder()
                    .currencyCode(currencyCode)
                    .value(value)
                    .build();
            updatedCurrencyMap.put(currencyCode, currency);
        }
        List<String> currencyMapKeys = new ArrayList<>(updatedCurrencyMap.keySet());
        List<String> supportedCurrenciesKeys = SupportedCurrencies.SUPPORTED_CURRENCIES.stream().map(Currency::getCurrencyCode).toList();
        if (supportedCurrenciesKeys.isEmpty()) {
            return updatedCurrencyMap;
        }
        Map<String, Currency> filteredCurrencyMap = new HashMap<>();
        for (String currencyCode : supportedCurrenciesKeys) {
            if (currencyMapKeys.contains(currencyCode)) {
                filteredCurrencyMap.put(currencyCode, updatedCurrencyMap.get(currencyCode));
            }
        }
        return filteredCurrencyMap;
    }
    public Map<String, Double> getCurrencyMap() {
        Map<String, Double> currencyMap = new HashMap<>();
        this.fetchResponse();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(this.getResponse());
            JsonNode ratesNode = rootNode.path("data").path("rates");
            boolean validNode = ratesNode != null && ratesNode.isObject();
            if (validNode) {
                Iterator<String> fieldNames = ratesNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String code = fieldNames.next();
                    double value = 1.0 / ratesNode.get(code).asDouble();
                    currencyMap.put(code, value);
                }
            } else {
                System.err.println("Error: Invalid data format received from API.");
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to parse JSON response", exception);
        }
        return currencyMap;
    }
}
