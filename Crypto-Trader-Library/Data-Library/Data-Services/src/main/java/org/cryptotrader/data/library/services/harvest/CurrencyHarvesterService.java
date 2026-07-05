package org.cryptotrader.data.library.services.harvest;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.data.library.component.CurrencyDataRetriever;
import org.cryptotrader.data.library.component.CurrencyJsonGenerator;
import org.cryptotrader.data.library.component.MarketSnapshotsBackfiller;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.entity.currency.SupportedCurrencies;
import org.cryptotrader.data.library.entity.currency.UniqueCurrency;
import org.cryptotrader.data.library.repository.CurrencyHistoryRepository;
import org.cryptotrader.data.library.repository.CurrencyRepository;
import org.cryptotrader.data.library.repository.UniqueCurrencyHistoryRepository;
import org.cryptotrader.data.library.repository.UniqueCurrencyRepository;
import org.cryptotrader.data.library.services.CurrencyService;
import org.cryptotrader.data.library.services.models.MarketSnapshotOperations;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@ConditionalOnProperty(name = "cryptotrader.harvest.currency", havingValue = "true", matchIfMissing = false)
public class CurrencyHarvesterService {
    //============================-Variables-=================================
    private final CurrencyRepository currencyRepository;
    private final CurrencyHistoryRepository currencyHistoryRepository;
    private final UniqueCurrencyRepository uniqueCurrencyRepository;
    private final UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository;
    private final CurrencyDataRetriever currencyDataRetriever;
    private final MarketSnapshotsBackfiller backfiller;
    private final MarketSnapshotOperations snapshotService;
    private final CurrencyJsonGenerator currencyJsonGenerator;
    private final CurrencyService currencyService;
    private final ObjectProvider<CurrencyHarvesterService> selfProvider;

    //===========================-Constructors-===============================
    @Autowired
    public CurrencyHarvesterService(CurrencyRepository currencyRepository,
                                    CurrencyHistoryRepository currencyHistoryRepository,
                                    UniqueCurrencyRepository uniqueCurrencyRepository,
                                    UniqueCurrencyHistoryRepository uniqueCurrencyHistoryRepository,
                                    CurrencyDataRetriever currencyDataRetriever,
                                    MarketSnapshotsBackfiller backfiller,
                                    MarketSnapshotOperations snapshotService,
                                    CurrencyJsonGenerator currencyJsonGenerator,
                                    CurrencyService currencyService,
                                    ObjectProvider<CurrencyHarvesterService> selfProvider) {
        this.currencyRepository = currencyRepository;
        this.currencyHistoryRepository = currencyHistoryRepository;
        this.uniqueCurrencyRepository = uniqueCurrencyRepository;
        this.uniqueCurrencyHistoryRepository = uniqueCurrencyHistoryRepository;
        this.currencyDataRetriever = currencyDataRetriever;
        this.backfiller = backfiller;
        this.snapshotService = snapshotService;
        this.currencyJsonGenerator = currencyJsonGenerator;
        this.currencyService = currencyService;
        this.selfProvider = selfProvider;
    }
    //============================-Methods-===================================

    //--------------------------Save-Currencies-------------------------------
    @Scheduled(fixedRate = 5000)
    public void runCurrencySavingSchedule() {
        this.self().saveCurrencies();
    }

    // TODO: Temporary implementation. This needs to be cleaned up and locking
    //       policy needs to be determined.
    @TimeTracked(expectedMillis = 5000, shouldPersist = true)
    public void saveCurrencies() {
        log.info("Updating currencies...");
        Map<String, Currency> currencies = this.currencyDataRetriever.getUpdatedCurrencies();
        String saveType = System.getProperty("cryptotrader.currency.save.type", "default");
        currencies.values().removeIf(Objects::isNull);
        log.info("Retrieved {} currencies from data source.", currencies.size());
        try {
            if (saveType.equalsIgnoreCase("batch")) {
                try {

                    List<Currency> currenciesToSave = new ArrayList<>();
                    List<UniqueCurrency> uniqueCurrenciesToSave = new ArrayList<>();
                    for (Currency currency : SupportedCurrencies.SUPPORTED_CURRENCIES) {
                        try {
                            Currency previousCurrency = Currency.from(currency);
                            String currencyCode = currency.getCurrencyCode();
                            Currency updatedCurrency = currencies.get(currencyCode);
                            currency.setValue(updatedCurrency.getValue());
                            currenciesToSave.add(currency);
                            if (this.currencyService.shouldSaveUniqueCurrency(currency, previousCurrency, updatedCurrency)) {
                                uniqueCurrenciesToSave.add(new UniqueCurrency(currency));
                            }
                        } catch (NullPointerException exception) {
                            log.error("Problematic currency: {}", currency.getCurrencyCode());
                            log.debug("Currency failure details: {}", exception.toString());
                        }
                    }
                    log.info("Successfully updated {} out of {} supported currencies.", currenciesToSave.size(), SupportedCurrencies.SUPPORTED_CURRENCIES.size());
                    this.currencyService.saveAllCurrencies(currenciesToSave);
                    this.currencyService.saveAllUniqueCurrencies(uniqueCurrenciesToSave);
                    this.snapshotService.saveSnapshot(currencies);
                } catch (NullPointerException exception) {
                    log.error("Failed to update currencies. Regenerating JSON. Error: ", exception);
                } catch (RuntimeException dbEx) {
                    log.warn("Database unavailable during currency update; skipping this cycle: {}", dbEx.getMessage());
                }
            } else {
                int numSuccessfullyUpdated = 0;
                for (Currency currency : SupportedCurrencies.SUPPORTED_CURRENCIES) {
                    try {
                        Currency previousCurrency = Currency.from(currency);
                        String currencyCode = currency.getCurrencyCode();
                        Currency updatedCurrency = currencies.get(currencyCode);
                        currency.setValue(updatedCurrency.getValue());
                        this.currencyService.saveCurrency(currency);
                        this.currencyService.saveUniqueCurrencyIfNew(currency, previousCurrency, updatedCurrency);
                        numSuccessfullyUpdated++;
                    } catch (NullPointerException exception) {
                        log.error("Problematic currency: {}", currency.getCurrencyCode());
                        log.debug("Currency failure details: {}", exception.toString());
                    }
                }
                log.info("Successfully updated {} out of {} supported currencies.", numSuccessfullyUpdated, SupportedCurrencies.SUPPORTED_CURRENCIES.size());
                this.snapshotService.saveSnapshot(currencies);
            }
        } catch (NullPointerException exception) {
            log.error("Failed to update currencies. Regenerating JSON. Error: ", exception);
            this.currencyJsonGenerator.generateAndSave();
        } catch (RuntimeException dbEx) {
            log.warn("Database unavailable during currency update; skipping this cycle: {}", dbEx.getMessage());
        }
    }

    @TimeTracked
    public void buildMarketSnapshots(boolean fullRefresh) {
        this.backfiller.buildSnapshots(fullRefresh);
    }

    private CurrencyHarvesterService self() {
        return this.selfProvider.getObject();
    }
}
