package org.cryptotrader.data.library.component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.data.library.entity.currency.SupportedCurrencies;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CurrencyJsonGenerator {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE_API_URL = "https://api.exchange.coinbase.com/currencies";
    private static final String CURRENCY_RATES_URL = "https://api.coinbase.com/v2/exchange-rates?currency=USD";
    private static final Path OUTPUT_PATH = Paths.get("src/main/resources/static/currencies.json");

    @Autowired
    public CurrencyJsonGenerator(final RestTemplate restTemplate, final ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private <T> @Nullable T fetchJson(final @NotNull String url, final @NotNull ParameterizedTypeReference<T> typeRef) {
        return this.restTemplate.exchange(url, HttpMethod.GET, null, typeRef).getBody();
    }

    public static void deleteExistingJson() {
        try {
            final boolean deleted = Files.deleteIfExists(OUTPUT_PATH);
            if (deleted) {
                log.info("Existing JSON file deleted at {}", OUTPUT_PATH);
            } else {
                log.info("No existing JSON file found at {}", OUTPUT_PATH);
            }
        } catch (final IOException exception) {
            throw new RuntimeException("Failed to delete existing JSON file at " + OUTPUT_PATH, exception);
        }
        SupportedCurrencies.clearCurrencies();
    }

    public @NotNull List<Map<String, Object>> getCurrencies() {
        final List<Map<String, Object>> currencies = fetchJson(EXCHANGE_API_URL,
                new ParameterizedTypeReference<>() { });

        final Map<String, Object> exchangeRatesRoot = fetchJson(CURRENCY_RATES_URL,
                new ParameterizedTypeReference<>() { });

        final Map<String, Object> data = asMap(exchangeRatesRoot.get("data"));
        final Map<String, Object> rates = asMap(data.get("rates"));

        final Set<String> currenciesToSkip = new HashSet<>(Arrays.asList(
                "DYP", "USD", "LQTY", "WLUNA", "GUSD",
                "DAI", "ME", "MASK", "USDC", "DAR",
                "AERGO", "TONE", "RAD", "NU"
        ));

        final List<Map<String, Object>> matchedCurrencies = new ArrayList<>();
        if (currencies != null) {
            for (final Map<String, Object> currency : currencies) {
                final String id = this.asString(currency.get("id"));
                final String name = this.asString(currency.get("name"));
                if (id == null || name == null) {
                    continue;
                }
                if (rates.containsKey(id)) {
                    final boolean containsDigit = id.chars().anyMatch(Character::isDigit);
                    if (!containsDigit && !currenciesToSkip.contains(id)) {
                        final Map<String, Object> entry = new HashMap<>();
                        entry.put("name", name);
                        entry.put("code", id);
                        matchedCurrencies.add(entry);
                    }
                }
            }
        }
        return matchedCurrencies;
    }

    public List<Map<String, Object>> getCachedCurrencies() throws IOException {
        if (!Files.exists(OUTPUT_PATH)) {
            throw new NoSuchFileException(OUTPUT_PATH.toString());
        }
        final byte[] fileAsBytes = Files.readAllBytes(OUTPUT_PATH);
        return this.objectMapper.readValue(fileAsBytes, new TypeReference<>() { });
    }

    public void saveJson(final List<Map<String, Object>> matchedCryptos) {
        try {
            Files.createDirectories(OUTPUT_PATH.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(OUTPUT_PATH, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, matchedCryptos);
            }
            log.info("Matched cryptocurrencies saved to {}", OUTPUT_PATH);
        } catch (final IOException exception) {
            throw new RuntimeException("Failed to save JSON to " + OUTPUT_PATH, exception);
        }
    }

    public @NotNull List<String> getAllCurrencyCodes(final boolean useCache) {
        List<Map<String, Object>> currencies;
        if (useCache) {
            try {
                currencies = this.getCachedCurrencies();
            } catch (final NoSuchFileException e) {
                log.error("Cache file not found. Fetching currencies from API.");
                currencies = this.getCurrencies();
            } catch (final IOException e) {
                log.error("Failed to read cache. Fetching currencies from API.");
                currencies = this.getCurrencies();
            }
        } else {
            currencies = getCurrencies();
        }

        final List<String> codes = currencies.stream()
                .map(codeMap -> asString(codeMap.get("code")))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("Number of currencies found: {}", codes.size());
        return codes;
    }

    public void generateAndSave() {
        deleteExistingJson();
        this.saveJson(this.getCurrencies());
        SupportedCurrencies.loadCurrenciesFromJson();
    }

    @SuppressWarnings("unchecked")
    private @NotNull Map<String, Object> asMap(final Object object) {
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        return Collections.emptyMap();
    }

    private String asString(final @Nullable Object object) {
        if (object == null) {
            return null;
        }
        return String.valueOf(object);
    }

    public static @NotNull CurrencyJsonGenerator standalone() {
        return new CurrencyJsonGenerator(new RestTemplate(), new ObjectMapper());
    }
}