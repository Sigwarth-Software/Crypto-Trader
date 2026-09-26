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
import org.jspecify.annotations.NonNull;
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
    private final CurrencyRepository currencyRepository;
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository;

    private final CurrencyEntityService currencyEntityService;
    private final CurrencyHistoryEntityService currencyHistoryEntityService;
    private final UniqueCurrencyEntityService uniqueCurrencyEntityService;
    private final UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService;

    //===========================-Constructors-===============================
    @Autowired
    public CurrencyService(final CurrencyRepository currencyRepository,
                           final CurrencyHistoryRepository currencyHistoryRepository,
                           final UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository,
                           final CurrencyEntityService currencyEntityService,
                           final CurrencyHistoryEntityService currencyHistoryEntityService,
                           final UniqueCurrencyEntityService uniqueCurrencyEntityService,
                           final UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService) {
        this.currencyRepository = currencyRepository;
        this.currencyHistoryRepository = currencyHistoryRepository;
        this.uniqueCurrencyHistoryRepository = uniqueCurrencyHistoryRepository;
        this.currencyEntityService = currencyEntityService;
        this.currencyHistoryEntityService = currencyHistoryEntityService;
        this.uniqueCurrencyEntityService = uniqueCurrencyEntityService;
        this.uniqueCurrencyHistoryEntityService = uniqueCurrencyHistoryEntityService;
    }


    public TimeValueResponse getFuzzyCurrencyHistory(String currencyCode, FuzzyTimeValueRequest request) {
        final LocalDateTime requestedTime = LocalDateTime.parse(request.getDateTime());
        final CurrencyHistory closestRecord = this.currencyHistoryRepository.findClosestCurrencyHistoryByCurrencyCode(currencyCode, requestedTime);
        if (closestRecord == null) {
            return null;
        }
        return new TimeValueResponse(closestRecord.getLastUpdated().toString(), closestRecord.getValue());
    }

    public DisplayCurrencyResponse toCurrencyValueResponse(Currency currency) {
        if (currency == null) {
            return null;
        }
        return new DisplayCurrencyResponse(currency.getName(),
                currency.getCurrencyCode(),
                currency.getValue());
    }

    public DisplayCurrencyListResponse getCurrencyValuesResponse() {
        final List<Currency> currencies = this.getTopTenNonEncapsulatedCurrencies();
        currencies.sort((currencyOne, currencyTwo) ->
            Double.compare(currencyTwo.getValue(), currencyOne.getValue()));

        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public DisplayCurrencyListResponse getCurrencyValuesResponse(final int offset) {
        final List<Currency> currencies = this.getTopNonEncapsulatedCurrencies(offset);
        currencies.sort((currencyOne, currencyTwo) -> Double.compare(currencyTwo.getValue(), currencyOne.getValue()));
        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public List<Currency> getTopNonEncapsulatedCurrencies(final int offset) {
        final int pageSize = 10;
        final int safeOffset = Math.max(0, offset);
        final int page = safeOffset / pageSize;
        final Pageable pageable = PageRequest.of(page, pageSize);
        return this.currencyRepository.findNonEncapsulated(pageable);
    }

    public List<Currency> getTopTenNonEncapsulatedCurrencies() {
        return this.currencyRepository.findTopTenNonEncapsulated();
    }

    public List<Currency> getTopTenCurrencies() {
        return this.currencyRepository.findTop10ByOrderByValueDesc();
    }

    public List<String> getCurrencyNames(final boolean withCode) {
        return this.getAllCurrencies().stream().map(currency ->
            this.getCurrencyName(withCode, currency)).toList();
    }

    public String getCurrencyName(final boolean withCode,
                                  final Currency currency) {
        String nameString = currency.getName();
        if (withCode) {
            nameString = nameString + " (" + currency.getCurrencyCode() + ")";
        }
        return nameString;
    }

    public List<Currency> getAllCurrencies() {
        return this.currencyEntityService.findAll();
    }

    public List<String> getAllCurrencyCodes() {
        return this.currencyRepository.findAllCurrencyCodes();
    }

    public void saveCurrencyIfNew(final Currency currency,
                                  final Currency previousCurrency,
                                  final Currency updatedCurrency) {
        if (this.hasCurrencyChanged(previousCurrency, updatedCurrency)) {
            this.saveCurrency(currency);
        }
    }

    private boolean hasCurrencyChanged(final Currency previousCurrency,
                                       final Currency updatedCurrency) {
        if (previousCurrency == null || updatedCurrency == null) {
            return true;
        }
        return previousCurrency.getValue() != updatedCurrency.getValue();
    }

    public void saveCurrency(final Currency currency) {
        this.currencyEntityService.save(currency);
        this.currencyHistoryEntityService.save(new CurrencyHistory(currency, currency.getValue()));
    }

    public void saveAllCurrencies(final List<Currency> currencies) {
        this.currencyEntityService.saveAll(currencies);
        this.currencyHistoryEntityService.saveAll(currencies.stream()
                .map(currency -> new CurrencyHistory(currency, currency.getValue()))
                .collect(Collectors.toList()));
    }

    public void saveAllUniqueCurrencies(final List<UniqueCurrency> uniqueCurrencies) {
        this.uniqueCurrencyEntityService.saveAll(uniqueCurrencies);
        this.uniqueCurrencyHistoryEntityService.saveAll(uniqueCurrencies.stream()
                .map(uniqueCurrency -> new UniqueCurrencyHistory(uniqueCurrency.getAssociatedCurrency()))
                .collect(Collectors.toList()));
    }

    public void saveUniqueCurrencyIfNew(final Currency currency,
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

    public boolean shouldSaveUniqueCurrency(final Currency currency, final Currency previousCurrency, final Currency updatedCurrency) {
        if (!this.existsInUniqueCurrencyTable(currency.getCurrencyCode())) {
            return true;
        }
        return this.hasCurrencyChanged(previousCurrency, updatedCurrency);
    }

    public void saveUniqueCurrency(final Currency currency) {
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
    public boolean existsInCurrencyTable(final String currencyCode) {
        return this.currencyEntityService.existsById(currencyCode);
    }
    public boolean existsInCurrencyHistoryTable(final String currencyCode) {
        return this.currencyHistoryRepository.existsByCurrencyCurrencyCode(currencyCode);
    }
    public boolean existsInUniqueCurrencyTable(final String currencyCode) {
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

    public PerformanceRating getDayPerformance(final String currencyCode) {
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

    public String getPercentageDayPerformance(final String currencyCode) {
        final Currency currency = this.getCurrencyByCurrencyCode(currencyCode);
        if (currency == null) {
            return "0.00%";
        }
        double currentPrice = currency.getValue();
        CurrencyHistory previousDay = this.currencyHistoryRepository.getPreviousDayCurrency(currencyCode);
        if (previousDay == null) {
            return "0.00%";
        }
        double lastDayPrice = previousDay.getValue();
        if (lastDayPrice == 0.0 || Double.isNaN(lastDayPrice) || Double.isNaN(currentPrice)) {
            return "0.00%";
        }
        double percentDelta = (currentPrice - lastDayPrice) / lastDayPrice * 100.0;
        return String.format("%+.2f%%", percentDelta);
    }

    public List<TimeValueResponse> getCurrencyHistory(final String currencyCode,
                                                      final int hours,
                                                      int intervalSeconds) {
        if (intervalSeconds <= 0) {
            intervalSeconds = 60;
        }
        final LocalDateTime since = LocalDateTime.now().minusHours(hours);
        final List<Object[]> rows = this.currencyHistoryRepository.findDownsampledHistory(currencyCode, since, intervalSeconds);
        return rows.stream()
                .map(record -> {
                    Object time = record[0];
                    LocalDateTime localDateTime = null;
                    if (time instanceof final Timestamp timestamp) {
                        localDateTime = timestamp.toLocalDateTime();
                    } else if (time instanceof final LocalDateTime dateTime) {
                        localDateTime = dateTime;
                    } else if (time instanceof final Instant instant) {
                        localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    } else if (time instanceof final OffsetDateTime offsetDateTime) {
                        localDateTime = offsetDateTime.toLocalDateTime();
                    } else if (time instanceof final ZonedDateTime zonedDateTime) {
                        localDateTime = zonedDateTime.toLocalDateTime();
                    }

                    final String isoTime = localDateTime != null ? localDateTime.toString() : String.valueOf(time);
                    final double valueAtTime = ((Number) record[1]).doubleValue();
                    return new TimeValueResponse(isoTime, valueAtTime);
                })
                .toList();
    }

    public List<String> getTopCurrenciesByPerformance(final int topCount) {
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
                    return this.getCurrencyName(
                        true,
                        currency
                    ) + " [N/A]";
                }

                final String currencyPerformanceScoreString = this.getCurrencyPerformanceScoreString(currencyPerformanceScore);

                return this.getCurrencyName(
                    true,
                    currency
                ) + " [" + currencyPerformanceScoreString + "%]";
            })
            .toList();
    }

    private @NonNull String getCurrencyPerformanceScoreString(final Double currencyPerformanceScore) {
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

    public Double getCurrencyPerformanceScore(final Currency currency) {
        final String percentageDayPerformance =
            this.getPercentageDayPerformance(currency.getCurrencyCode());

        try {
            return Double.parseDouble(
                percentageDayPerformance.replaceFirst("%$", "")
            );
        } catch (final NumberFormatException exception) {
            return null;
        }
    }
}
