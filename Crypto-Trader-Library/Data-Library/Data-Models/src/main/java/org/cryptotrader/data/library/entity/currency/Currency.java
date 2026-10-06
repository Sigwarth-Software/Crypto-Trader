package org.cryptotrader.data.library.entity.currency;
//=================================-Imports-==================================

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.cryptotrader.data.library.model.http.ApiDataRetriever;
import org.cryptotrader.data.library.entity.currency.builder.CurrencyBuilder;
import org.cryptotrader.universal.library.entity.Identifiable;
import org.cryptotrader.universal.library.model.annotation.Loggable;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "currencies")
public class Currency extends Identifiable<String> {
    //============================-Variables-=================================
    @Column(name = "currency_name")
    @Loggable
    private String name;
    @Id
    @Column(name = "currency_code")
    @Loggable
    private String currencyCode;
    @Transient
    private String urlPath;
    @Column(name = "currency_value", columnDefinition = "DECIMAL(34, 18)")
    @Loggable
    private double value;
    @JsonIgnore
    @Transient
    private static final DecimalFormat decimalFormat = new DecimalFormat("##,#00.00000000");
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    public static @NotNull String TESTING_URL = "test";
    //===========================-Constructors-===============================
    public Currency() {
        this.name = "";
        this.currencyCode = "";
        this.urlPath = "";
        this.value = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(final String name, final String currencyCode) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = this.getCoinbaseUrl();
        this.value = this.getApiValue();
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(final String name, final String currencyCode, final String urlPath) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = urlPath;
        this.value = this.getApiValue();
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(final String name, final String currencyCode, final double value, final String urlPath) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.value = value;
        this.urlPath = urlPath;
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(final String name, final String currencyCode, final String urlPath, final double value, final LocalDateTime lastUpdated) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = urlPath;
        this.value = value;
        this.lastUpdated = lastUpdated;
    }
    //=============================-Methods-==================================

    public @NotNull String getCoinbaseUrl() {
        return "https://api.coinbase.com/v2/prices/%s-USD/spot".formatted(this.currencyCode);
    }
    //----------------------------Update-Value--------------------------------
    public void updateValue() {
        this.lastUpdated = LocalDateTime.now();
        this.value = this.getApiValue();
    }
    //---------------------------Get-Api-Value--------------------------------
    public double getApiValue() {
        final String currencyApiJson = this.getCurrencyApiJson();
        final boolean isEmpty = currencyApiJson == null || currencyApiJson.isEmpty();
        final boolean isNoDataString = currencyApiJson.equals(ApiDataRetriever.NO_DATA_ERROR_MESSAGE);
        if (isNoDataString || isEmpty) {
            final String errorMessage = "No data received from API for %s. Aborting Currency construction.".formatted(this.currencyCode);
            throw new IllegalStateException(errorMessage);
        }
        return this.getValueFromJson(currencyApiJson);
    }
    //----------------------------Format-Value--------------------------------
    public @NotNull String formatValue(final double value) {
        String reformattedValue = decimalFormat.format(value);
        if (reformattedValue == null) {
            reformattedValue = "";
        }
        return reformattedValue;
    }
    //-----------------------Get-Currency-Api-Json----------------------------
    public String getCurrencyApiJson() {
        final ApiDataRetriever apiDataRetriever = new ApiDataRetriever(this.urlPath);
        return apiDataRetriever.getResponse();
    }
    //------------------------Get-Value-From-Json-----------------------------
    public double getValueFromJson(final @NotNull String json) {
        final StringBuilder currencyJson = new StringBuilder(json);
        final String amountKey = "amount\":\"";
        final int amountLength = amountKey.length();
        final int indexOfAmount = currencyJson.indexOf(amountKey) + amountLength;
        final int endIndex = currencyJson.length();
        currencyJson.delete(0, indexOfAmount);
        final int indexOfCloseQuote = currencyJson.indexOf("\"");
        currencyJson.delete(indexOfCloseQuote, endIndex);
        final String currencyValueString = currencyJson.toString();
        final double currencyValue = Double.parseDouble(currencyValueString);
        return currencyValue;
    }
    //-------------------------Get-Updated-Value------------------------------
    public double getUpdatedValue() {
        if (this.urlPath == null || this.urlPath.isEmpty()) {
            this.urlPath = this.getCoinbaseUrl();
        }
        if (this.urlPath.equals(TESTING_URL)) {
            return this.value;
        }
        this.updateValue();
        return this.value;
    }

    public static @NotNull Currency fromExisting(final String currencyCode) {
        final Set<Currency> currencies = SupportedCurrencies.SUPPORTED_CURRENCIES;
        return currencies.stream()
            .filter(currency -> currency.getCurrencyCode().equalsIgnoreCase(currencyCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Currency with code " + currencyCode + " not found."));
    }

    public static @NotNull Currency fromHistory(final @NotNull CurrencyHistory currencyHistory) {
        return Currency.builder()
            .name(currencyHistory.getName())
            .currencyCode(currencyHistory.getCurrency().getCurrencyCode())
            .urlPath(currencyHistory.getCurrency().getUrlPath())
            .value(currencyHistory.getValue())
            .build();
    }

    public static @NotNull Currency from(final @NotNull Currency currency) {
        final Currency newCurrency = new Currency(currency.getName(), currency.getCurrencyCode(),
            currency.getUrlPath(), currency.getValue(),
            currency.getLastUpdated());
        return newCurrency;
    }
    //============================-Overrides-=================================

    //------------------------------Equals------------------------------------
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof final @NotNull Currency comparedCurrency) {
            final boolean sameName = this.name.equals(comparedCurrency.name);
            final boolean sameCode = this.currencyCode.equals(comparedCurrency.currencyCode);
            final boolean sameValue = this.value == comparedCurrency.value;
            final boolean sameUrl = this.urlPath.equals(comparedCurrency.urlPath);
            final boolean sameLastUpdated = this.lastUpdated.equals(comparedCurrency.lastUpdated);
            return sameName && sameCode && sameValue && sameUrl && sameLastUpdated;
        }
        return false;
    }
    //------------------------------Hash-Code---------------------------------

    //------------------------------To-String---------------------------------
    @Override
    public @NotNull String toString() {
        final String currencyString = """
                %18s --- %5s - %16s""".formatted(this.name, this.currencyCode,
            "$" + decimalFormat.format(this.value));
        return currencyString;
    }
    public static @NotNull CurrencyBuilder builder() {
        return new CurrencyBuilder();
    }
    //=============================-Getters-==================================
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }
    //=============================-Setters-==================================
    public void setValue(final double value) {
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return this.currencyCode;
    }

    @Override
    public void setId(final String id) {
        this.currencyCode = id;
    }
}
