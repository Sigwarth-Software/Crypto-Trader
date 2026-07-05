package org.cryptotrader.data.library.entity.currency;
//=================================-Imports-==================================
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class SupportedCurrencies {
    //============================-Constants-=================================
    public static final Set<Currency> SUPPORTED_CURRENCIES = new HashSet<>();
    private static final Map<String, Currency> CURRENCY_MAP = new HashMap<>();
    private static final String JSON_FILE_PATH = "src/main/resources/static/currencies.json";
    public static final Currency BITCOIN = new Currency("Bitcoin", "BTC");
    // TODO: This should be a config property, not hardcoded.
    private static final int MAX_CURRENCIES = 500;
    //==========================-Static-Actions-==============================
    static {
        boolean loadCurrencies = Boolean.parseBoolean(System.getProperty("cryptotrader.load.currency", "false"));
        if (loadCurrencies) {
            loadCurrenciesFromJson();
        } else {
            log.debug("SupportedCurrencies static load skipped (cryptotrader.load.currency=false).");
        }
    }

    public static void popCurrency(String code) {
        Currency currency = CURRENCY_MAP.remove(code);
        if (currency != null) {
            SUPPORTED_CURRENCIES.remove(currency);
            log.info("Popped currency: {} ({})", currency.getName(), code);
        } else {
            log.warn("Currency with code {} not found to pop.", code);
        }
    }

    public static void clearCurrencies() {
        SUPPORTED_CURRENCIES.clear();
        CURRENCY_MAP.clear();
    }

    public static void loadCurrenciesFromJson() {
        loadCurrenciesFromJson(JSON_FILE_PATH);
    }

    public static void loadCurrenciesFromJson(final String jsonPath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File jsonFile = new File(jsonPath);
            if (!jsonFile.exists()) {
                log.error("Currency JSON file not found. Using empty list.");
                return;
            }
            loadCurrenciesFromJson(objectMapper, jsonFile);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to load currencies from JSON", exception);
        }
    }

    private static void loadCurrenciesFromJson(ObjectMapper objectMapper, File jsonFile) throws IOException {
        List<Map<String, String>> currencyData = objectMapper.readValue(jsonFile, new TypeReference<>() {});
        for (Map<String, String> entry : currencyData) {
            String name = entry.get("name");
            String code = entry.get("code");
            if (code != null && !code.isEmpty() && Character.isDigit(code.charAt(0))) {
                log.warn("Skipping currency with invalid code (starts with digit): {} ({})", name, code);
                continue;
            }
            Currency currency;
            try {
                currency = new Currency(name, code);
            } catch (IllegalStateException exception) {
                continue;
            }
            if (name.equals("Bitcoin")) {
                SUPPORTED_CURRENCIES.add(BITCOIN);
                CURRENCY_MAP.put(code, BITCOIN);
            } else {
                SUPPORTED_CURRENCIES.add(currency);
                CURRENCY_MAP.put(code, currency);
            }
            log.info("Loaded currency: {} ({})", name, code);
        }
        log.info("Loaded {} currencies from JSON.", SUPPORTED_CURRENCIES.size());
        if (SUPPORTED_CURRENCIES.size() > MAX_CURRENCIES) {
            log.warn("Loaded currency count ({}) exceeds maximum ({}).", SUPPORTED_CURRENCIES.size(), MAX_CURRENCIES);
        }
    }
}
