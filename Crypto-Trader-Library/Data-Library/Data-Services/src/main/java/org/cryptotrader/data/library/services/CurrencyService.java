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
import org.cryptotrader.data.library.repository.UniqueCurrencyRepository;
import org.cryptotrader.data.library.services.entity.CurrencyEntityService;
import org.cryptotrader.data.library.services.entity.CurrencyHistoryEntityService;
import org.cryptotrader.data.library.services.entity.UniqueCurrencyEntityService;
import org.cryptotrader.data.library.services.entity.UniqueCurrencyHistoryEntityService;
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
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CurrencyService {
    //============================-Variables-=================================
    private final CurrencyRepository currencyRepository;
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final UniqueCurrencyRepository uniqueCurrencyRepository;
    private final UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository;

    private final CurrencyEntityService currencyEntityService;
    private final CurrencyHistoryEntityService currencyHistoryEntityService;
    private final UniqueCurrencyEntityService uniqueCurrencyEntityService;
    private final UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService;

    //===========================-Constructors-===============================
    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository,
                           CurrencyHistoryRepository currencyHistoryRepository,
                           UniqueCurrencyRepository uniqueCurrencyRepository,
                           UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository,
                           CurrencyEntityService currencyEntityService,
                           CurrencyHistoryEntityService currencyHistoryEntityService,
                           UniqueCurrencyEntityService uniqueCurrencyEntityService,
                           UniqueCurrencyHistoryEntityService uniqueCurrencyHistoryEntityService) {
        this.currencyRepository = currencyRepository;
        this.currencyHistoryRepository = currencyHistoryRepository;
        this.uniqueCurrencyRepository = uniqueCurrencyRepository;
        this.uniqueCurrencyHistoryRepository = uniqueCurrencyHistoryRepository;
        this.currencyEntityService = currencyEntityService;
        this.currencyHistoryEntityService = currencyHistoryEntityService;
        this.uniqueCurrencyEntityService = uniqueCurrencyEntityService;
        this.uniqueCurrencyHistoryEntityService = uniqueCurrencyHistoryEntityService;
    }


    public TimeValueResponse getFuzzyCurrencyHistory(String currencyCode, FuzzyTimeValueRequest request) {
        LocalDateTime requestedTime = LocalDateTime.parse(request.getDateTime());
        CurrencyHistory closestRecord = this.currencyHistoryRepository.findClosestCurrencyHistoryByCurrencyCode(currencyCode, requestedTime);
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
//        List<Currency> currencies = this.getTopTenCurrencies();
        List<Currency> currencies = this.getTopTenNonEncapsulatedCurrencies();
        // sort by price desc
        currencies.sort((currencyOne, currencyTwo) -> {
            return Double.compare(currencyTwo.getValue(), currencyOne.getValue());
        });
        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public DisplayCurrencyListResponse getCurrencyValuesResponse(int offset) {
        List<Currency> currencies = this.getTopNonEncapsulatedCurrencies(offset);
        currencies.sort((currencyOne, currencyTwo) -> Double.compare(currencyTwo.getValue(), currencyOne.getValue()));
        return new DisplayCurrencyListResponse(currencies.stream()
                .filter(Objects::nonNull)
                .map(this::toCurrencyValueResponse)
                .filter(Objects::nonNull)
                .toList());
    }

    public List<Currency> getTopNonEncapsulatedCurrencies(int offset) {
        int pageSize = 10;
        int safeOffset = Math.max(0, offset);
        int page = safeOffset / pageSize;
        Pageable pageable = PageRequest.of(page, pageSize);
        return this.currencyRepository.findNonEncapsulated(pageable);
    }

    public List<Currency> getTopTenNonEncapsulatedCurrencies() {
        return this.currencyRepository.findTopTenNonEncapsulated();
    }

    public List<Currency> getTopTenCurrencies() {
        return this.currencyRepository.findTop10ByOrderByValueDesc();
    }

    public List<String> getCurrencyNames(boolean withCode) {
        return this.getAllCurrencies().stream().map(currency -> {
            return this.getCurrencyName(withCode, currency);
        }).toList();
    }

    public String getCurrencyName(boolean withCode, Currency currency) {
        String nameString = currency.getName();
        if (withCode) {
            nameString = nameString + " (" + currency.getCurrencyCode() + ")";
        }
        return nameString;
    }

    public List<Currency> getAllCurrencies() {
//        return this.currencyRepository.findAll();
        return this.currencyEntityService.findAll();
    }

    public List<String> getAllCurrencyCodes() {
        return this.currencyRepository.findAllCurrencyCodes();
    }

    public void saveCurrencyIfNew(Currency currency, Currency previousCurrency, Currency updatedCurrency) {
        if (this.hasCurrencyChanged(previousCurrency, updatedCurrency)) {
            this.saveCurrency(currency);
        }
    }

    private boolean hasCurrencyChanged(Currency previousCurrency, Currency updatedCurrency) {
        if (previousCurrency == null || updatedCurrency == null) {
            return true;
        }
        return previousCurrency.getValue() != updatedCurrency.getValue();
    }

    public void saveCurrency(Currency currency) {
        this.currencyEntityService.save(currency);
        this.currencyHistoryEntityService.save(new CurrencyHistory(currency, currency.getValue()));
    }

    public void saveUniqueCurrencyIfNew(Currency currency, Currency previousCurrency, Currency updatedCurrency) {
        if (!this.existsInUniqueCurrencyTable(currency.getCurrencyCode())) {
            this.saveUniqueCurrency(currency);
            return;
        }
        if (this.hasCurrencyChanged(previousCurrency, updatedCurrency)) {
            this.saveUniqueCurrency(currency);
        }
    }

    public void saveUniqueCurrency(Currency currency) {
        UniqueCurrency uniqueCurrency = new UniqueCurrency(currency);
        UniqueCurrencyHistory uniqueCurrencyHistory = new UniqueCurrencyHistory(currency);
//        this.uniqueCurrencyRepository.save(uniqueCurrency);
//        this.uniqueCurrencyHistoryRepository.save(uniqueCurrencyHistory);

        this.uniqueCurrencyEntityService.save(uniqueCurrency);
        this.uniqueCurrencyHistoryEntityService.save(uniqueCurrencyHistory);

    }

    //------------------------Get-Currency-By-Name----------------------------
    public Currency getCurrencyByName(String currencyName) {
        return this.currencyRepository.getCurrencyByName(currencyName);
    }
    //-------------------Get-Currency-By-Currency-Code------------------------
    public Currency getCurrencyByCurrencyCode(String currencyCode) {
        return this.currencyRepository.getCurrencyByCurrencyCode(currencyCode);
    }
    public boolean existsInCurrencyTable(String currencyCode) {
//        return this.currencyRepository.existsByCurrencyCode(currencyCode);
        return this.currencyEntityService.existsById(currencyCode);
    }
    public boolean existsInCurrencyHistoryTable(String currencyCode) {
        return this.currencyHistoryRepository.existsByCurrencyCurrencyCode(currencyCode);
    }
    public boolean existsInUniqueCurrencyTable(String currencyCode) {
//        return this.uniqueCurrencyRepository.existsByCurrency(currencyCode);
        return this.uniqueCurrencyEntityService.existsById(currencyCode);
    }
    public boolean existsInUniqueCurrencyHistoryTable(String currencyCode) {
        return this.uniqueCurrencyHistoryRepository.existsByCurrencyCurrencyCode(currencyCode);
    }

    public List<TimeValueResponse> getCurrencyHistory(String currencyCode, int hours) {
        return this.getCurrencyHistory(currencyCode, hours, 60);
    }

    public PerformanceRating getDayPerformance(String currencyCode) {
        Currency currency = this.getCurrencyByCurrencyCode(currencyCode);
        if (currency == null) {
            return PerformanceRating.NEUTRAL;
        }
        double currentPrice = currency.getValue();
        CurrencyHistory previousDay = this.currencyHistoryRepository.getPreviousDayCurrency(currencyCode);
        if (previousDay == null) {
            return PerformanceRating.NEUTRAL;
        }
        double lastDayPrice = previousDay.getValue();
        return PerformanceRating.fromValues(lastDayPrice, currentPrice);
    }

    public String getPercentageDayPerformance(String currencyCode) {
        Currency currency = this.getCurrencyByCurrencyCode(currencyCode);
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

    public List<TimeValueResponse> getCurrencyHistory(String currencyCode, int hours, int intervalSeconds) {
        if (intervalSeconds <= 0) {
            intervalSeconds = 60;
        }
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Object[]> rows = this.currencyHistoryRepository.findDownsampledHistory(currencyCode, since, intervalSeconds);
        return rows.stream()
                .map(record -> {
                    Object time = record[0];
                    LocalDateTime localDateTime = null;
                    if (time instanceof Timestamp timestamp) {
                        localDateTime = timestamp.toLocalDateTime();
                    } else if (time instanceof LocalDateTime dateTime) {
                        localDateTime = dateTime;
                    } else if (time instanceof Instant instant) {
                        localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    } else if (time instanceof OffsetDateTime offsetDateTime) {
                        localDateTime = offsetDateTime.toLocalDateTime();
                    } else if (time instanceof ZonedDateTime zonedDateTime) {
                        localDateTime = zonedDateTime.toLocalDateTime();
                    }

                    String isoTime = localDateTime != null ? localDateTime.toString() : String.valueOf(time);
                    double valueAtTime = ((Number) record[1]).doubleValue();
                    return new TimeValueResponse(isoTime, valueAtTime);
                })
                .toList();
    }
}
