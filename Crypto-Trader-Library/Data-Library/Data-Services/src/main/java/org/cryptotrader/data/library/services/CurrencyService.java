package org.cryptotrader.data.library.services;
//=================================-Imports-==================================
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.api.library.communication.request.FuzzyTimeValueRequest;
import org.cryptotrader.api.library.communication.response.DisplayCurrencyListResponse;
import org.cryptotrader.api.library.communication.response.DisplayCurrencyResponse;
import org.cryptotrader.universal.library.communication.response.TimeValueResponse;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.currency.CurrencyHistory;
import org.cryptotrader.data.library.entity.currency.UniqueCurrency;
import org.cryptotrader.data.library.entity.currency.UniqueCurrencyHistory;
import org.cryptotrader.data.library.model.currency.PerformanceRating;
import org.cryptotrader.data.library.repository.CurrencyHistoryRepository;
import org.cryptotrader.data.library.repository.CurrencyRepository;
import org.cryptotrader.data.library.repository.UniqueCurrencyHistoryRepository;
import org.cryptotrader.data.library.services.entity.CurrencyEntityService;
import org.cryptotrader.data.library.services.entity.CurrencyHistoryEntityService;
import org.cryptotrader.data.library.services.entity.UniqueCurrencyEntityService;
import org.cryptotrader.data.library.services.entity.UniqueCurrencyHistoryEntityService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CurrencyService {
    //============================-Variables-=================================
    private final @NotNull CurrencyRepository currencyRepository;
    private final @NotNull CurrencyHistoryRepository currencyHistoryRepository;
    private final @NotNull UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository;
    private final @NotNull CurrencyEntityService currencyEntityService;
    private final @NotNull CurrencyHistoryEntityService currencyHistoryEntityService;
    private final @NotNull UniqueCurrencyEntityService uniqueCurrencyEntityService;
    private final @NotNull UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService;

    //===========================-Constructors-===============================
    @Autowired
    public CurrencyService(@NotNull final CurrencyRepository currencyRepository,
                           @NotNull final CurrencyHistoryRepository currencyHistoryRepository,
                           @NotNull final UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository,
                           @NotNull final CurrencyEntityService currencyEntityService,
                           @NotNull final CurrencyHistoryEntityService currencyHistoryEntityService,
                           @NotNull final UniqueCurrencyEntityService uniqueCurrencyEntityService,
                           @NotNull final UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService) {
        this.currencyRepository = currencyRepository;
        this.currencyHistoryRepository = currencyHistoryRepository;
        this.uniqueCurrencyHistoryRepository = uniqueCurrencyHistoryRepository;
        this.currencyEntityService = currencyEntityService;
        this.currencyHistoryEntityService = currencyHistoryEntityService;
        this.uniqueCurrencyEntityService = uniqueCurrencyEntityService;
        this.uniqueCurrencyHistoryEntityService = uniqueCurrencyHistoryEntityService;
    }


    public @Nullable TimeValueResponse getFuzzyCurrencyHistory(@NotNull final String currencyCode,
                                                               @NotNull final FuzzyTimeValueRequest request) {
        final LocalDateTime requestedTime = LocalDateTime.parse(
            request.getDateTime()
        );
        final CurrencyHistory closestRecord =
            this.currencyHistoryRepository.findClosestCurrencyHistoryByCurrencyCode(
                currencyCode,
                requestedTime
            );

        if (closestRecord == null) {
            return null;
        }
        return new TimeValueResponse(closestRecord.getLastUpdated().toString(), closestRecord.getValue());
    }

    public @Nullable DisplayCurrencyResponse toCurrencyValueResponse(@Nullable final Currency currency) {
        if (currency == null) {
            return null;
        }
        return new DisplayCurrencyResponse(currency.getName(),
                currency.getCurrencyCode(),
                currency.getValue());
    }

    public @NotNull DisplayCurrencyListResponse getCurrencyValuesResponse() {
        final List<Currency> currencies = this.getTopTenNonEncapsulatedCurrencies();
        currencies.sort((@NotNull final Currency currencyOne,
                         @NotNull final Currency currencyTwo) ->
            Double.compare(currencyTwo.getValue(), currencyOne.getValue()));

        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public @NotNull DisplayCurrencyListResponse getCurrencyValuesResponse(final int offset) {
        final List<Currency> currencies = this.getTopNonEncapsulatedCurrencies(offset);
        currencies.sort((currencyOne, currencyTwo) -> Double.compare(currencyTwo.getValue(), currencyOne.getValue()));
        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public List<Currency> getTopNonEncapsulatedCurrencies(final int offset) {
        final int DEFAULT_PAGE_SIZE = 10;
        final int safeOffset = Math.max(0, offset);
        final int page = safeOffset / DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return this.currencyRepository.findNonEncapsulated(pageable);
    }

    public List<Currency> getTopTenNonEncapsulatedCurrencies() {
        return this.currencyRepository.findTopTenNonEncapsulated();
    }

    public List<Currency> getTopTenCurrencies() {
        return this.currencyRepository.findTop10ByOrderByValueDesc();
    }

    public @NotNull List<String> getCurrencyNames(final boolean withCode) {
        return this.getAllCurrencies().stream().map(currency ->
            this.getCurrencyName(withCode, currency)).toList();
    }

    public String getCurrencyName(final boolean withCode,
                                  @NotNull final Currency currency) {
        String nameString = currency.getName();

        if (withCode) {
            nameString = "%s (%s)".formatted(
                nameString,
                currency.getCurrencyCode()
            );
        }
        return nameString;
    }

    public @NotNull List<Currency> getAllCurrencies() {
        return this.currencyEntityService.findAll();
    }

    public List<String> getAllCurrencyCodes() {
        return this.currencyRepository.findAllCurrencyCodes();
    }

    public void saveCurrencyIfNew(@NotNull final Currency currency,
                                  @Nullable final Currency previousCurrency,
                                  @Nullable final Currency updatedCurrency) {
        if (this.hasCurrencyChanged(previousCurrency, updatedCurrency)) {
            this.saveCurrency(currency);
        }
    }

    private boolean hasCurrencyChanged(@Nullable final Currency previousCurrency,
                                       @Nullable final Currency updatedCurrency) {
        if (previousCurrency == null || updatedCurrency == null) {
            return true;
        }
        return previousCurrency.getValue() != updatedCurrency.getValue();
    }

    public void saveCurrency(final @NotNull Currency currency) {
        this.currencyEntityService.save(currency);
        this.currencyHistoryEntityService.save(new CurrencyHistory(currency, currency.getValue()));
    }

    public void saveAllCurrencies(final @NotNull List<Currency> currencies) {
        this.currencyEntityService.saveAll(currencies);
        this.currencyHistoryEntityService.saveAll(currencies.stream()
                .map(currency -> new CurrencyHistory(currency, currency.getValue()))
                .collect(Collectors.toList()));
    }

    public void saveAllUniqueCurrencies(final @NotNull List<UniqueCurrency> uniqueCurrencies) {
        this.uniqueCurrencyEntityService.saveAll(uniqueCurrencies);
        this.uniqueCurrencyHistoryEntityService.saveAll(uniqueCurrencies.stream()
                .map(uniqueCurrency -> new UniqueCurrencyHistory(uniqueCurrency.getAssociatedCurrency()))
                .collect(Collectors.toList()));
    }

    public void saveUniqueCurrencyIfNew(final @NotNull Currency currency,
                                        final Currency previousCurrency,
                                        final Currency updatedCurrency) {
        if (!this.existsInUniqueCurrencyTable(currency.getCurrencyCode())) {
            this.saveUniqueCurrency(currency);
            return;
        }
        if (this.hasCurrencyChanged(previousCurrency, updatedCurrency)) {
            this.saveUniqueCurrency(currency);
        }
    }

    public boolean shouldSaveUniqueCurrency(final @NotNull Currency currency, final Currency previousCurrency, final Currency updatedCurrency) {
        if (!this.existsInUniqueCurrencyTable(currency.getCurrencyCode())) {
            return true;
        }
        return this.hasCurrencyChanged(previousCurrency, updatedCurrency);
    }

    public void saveUniqueCurrency(final @NotNull Currency currency) {
        final UniqueCurrency uniqueCurrency = new UniqueCurrency(currency);
        final UniqueCurrencyHistory uniqueCurrencyHistory = new UniqueCurrencyHistory(currency);
        this.uniqueCurrencyEntityService.save(uniqueCurrency);
        this.uniqueCurrencyHistoryEntityService.save(uniqueCurrencyHistory);

    }

    //------------------------Get-Currency-By-Name----------------------------
    public Currency getCurrencyByName(final String currencyName) {
        return this.currencyRepository.getCurrencyByName(currencyName);
    }
    //-------------------Get-Currency-By-Currency-Code------------------------
    public Currency getCurrencyByCurrencyCode(final String currencyCode) {
        return this.currencyRepository.getCurrencyByCurrencyCode(currencyCode);
    }
    public boolean existsInCurrencyTable(final @NotNull String currencyCode) {
        return this.currencyEntityService.existsById(currencyCode);
    }
    public boolean existsInCurrencyHistoryTable(final String currencyCode) {
        return this.currencyHistoryRepository.existsByCurrencyCurrencyCode(currencyCode);
    }
    public boolean existsInUniqueCurrencyTable(final @NotNull String currencyCode) {
        return this.uniqueCurrencyEntityService.existsById(currencyCode);
    }
    public boolean existsInUniqueCurrencyHistoryTable(final String currencyCode) {
        return this.uniqueCurrencyHistoryRepository.existsByCurrencyCurrencyCode(currencyCode);
    }

    public List<TimeValueResponse> getCurrencyHistory(final String currencyCode,
                                                      final int hours) {
        final int DEFAULT_INTERVAL_SECONDS = 60;
        return this.getCurrencyHistory(currencyCode, hours, DEFAULT_INTERVAL_SECONDS);
    }

    public @NotNull PerformanceRating getDayPerformance(final String currencyCode) {
        final Currency currency = this.getCurrencyByCurrencyCode(currencyCode);

        if (currency == null) {
            return PerformanceRating.NEUTRAL;
        }
        final double currentPrice = currency.getValue();
        final CurrencyHistory previousDay = this.currencyHistoryRepository.getPreviousDayCurrency(currencyCode);

        if (previousDay == null) {
            return PerformanceRating.NEUTRAL;
        }
        final double lastDayPrice = previousDay.getValue();
        return PerformanceRating.fromValues(lastDayPrice, currentPrice);
    }

    public @NotNull String getPercentageDayPerformance(@NotNull final String currencyCode) {
        final Currency currency = this.getCurrencyByCurrencyCode(currencyCode);

        if (currency == null) {
            return "0.00%";
        }
        final double currentPrice = currency.getValue();
        final CurrencyHistory previousDay = this.currencyHistoryRepository.getPreviousDayCurrency(currencyCode);

        if (previousDay == null) {
            return "0.00%";
        }

        final double lastDayPrice = previousDay.getValue();
        if (lastDayPrice == 0.0 || Double.isNaN(lastDayPrice) || Double.isNaN(currentPrice)) {
            return "0.00%";
        }
        final double percentDelta = (currentPrice - lastDayPrice) / lastDayPrice * 100.0;
        return String.format("%+.2f%%", percentDelta);
    }

    public @NotNull List<TimeValueResponse> getCurrencyHistory(final String currencyCode,
                                                               final int hours,
                                                               final int intervalSeconds) {
        final int normalizedIntervalSeconds = Math.max(intervalSeconds, 1);
        final LocalDateTime since = LocalDateTime.now().minusHours(hours);
        final List<Object[]> rows = this.currencyHistoryRepository.findDownsampledHistory(currencyCode, since, normalizedIntervalSeconds);
        return rows.stream()
                .map(record -> {
                    final Object time = record[0];
                    LocalDateTime localDateTime = null;

                    if (time instanceof final @NotNull Timestamp timestamp) {
                        localDateTime = timestamp.toLocalDateTime();
                    } else if (time instanceof final @NotNull LocalDateTime dateTime) {
                        localDateTime = dateTime;
                    } else if (time instanceof final @NotNull Instant instant) {
                        localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    } else if (time instanceof final @NotNull OffsetDateTime offsetDateTime) {
                        localDateTime = offsetDateTime.toLocalDateTime();
                    } else if (time instanceof final @NotNull ZonedDateTime zonedDateTime) {
                        localDateTime = zonedDateTime.toLocalDateTime();
                    }
                    final String isoTime = localDateTime != null ?
                        localDateTime.toString() : String.valueOf(time);
                    final double valueAtTime = ((Number) record[1]).doubleValue();
                    return new TimeValueResponse(isoTime, valueAtTime);
                })
                .toList();
    }

    public @NotNull List<String> getTopCurrenciesByPerformance(final int topCount) {
        return this.getAllCurrencies()
            .stream()
            .filter(Objects::nonNull)
            .sorted(
                Comparator
                    .comparing(
                        this::getCurrencyPerformanceScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                    )
                    .thenComparing(Currency::getCurrencyCode)
            )
            .limit(topCount)
            .map(currency -> {
                final Double currencyPerformanceScore =
                    this.getCurrencyPerformanceScore(currency);

                if (currencyPerformanceScore == null) {
                    return "%s [N/A]".formatted(this.getCurrencyName(
                        true,
                        currency
                    ));
                }

                final String currencyPerformanceScoreString = this.getCurrencyPerformanceScoreString(currencyPerformanceScore);

                return "%s [%s%%]".formatted(this.getCurrencyName(
                    true,
                    currency
                ), currencyPerformanceScoreString);
            })
            .toList();
    }

    private @NotNull String getCurrencyPerformanceScoreString(@NotNull final Double currencyPerformanceScore) {
        final boolean isPositive = currencyPerformanceScore >= 0;
        final boolean isNoChange = currencyPerformanceScore == 0.0;
        final String currencyPerformanceScoreString;

        if (isNoChange) {
            currencyPerformanceScoreString = "";
        } else if (isPositive) {
            currencyPerformanceScoreString = "+" + currencyPerformanceScore;
        } else {
            currencyPerformanceScoreString = "-" + currencyPerformanceScore;
        }
        return currencyPerformanceScoreString;
    }

    public @Nullable Double getCurrencyPerformanceScore(@NotNull final Currency currency) {
        final String percentageDayPerformance =
            this.getPercentageDayPerformance(currency.getCurrencyCode());

        try {
            return Double.parseDouble(
                percentageDayPerformance.replaceFirst("%$", "")
            );
        } catch (@NotNull final NumberFormatException exception) {
            return null;
        }
    }
}
