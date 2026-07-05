package org.cryptotrader.data.library.entity.currency;
//=================================-Imports-==================================

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.cryptotrader.data.library.model.http.ApiDataRetriever;
import org.cryptotrader.data.library.entity.currency.builder.CurrencyBuilder;
import org.cryptotrader.universal.library.entity.Identifiable;
import org.cryptotrader.universal.library.model.annotation.Loggable;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "currencies")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "currencyCode")
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
    public static String TESTING_URL = "test";
    //===========================-Constructors-===============================
    public Currency() {
        this.name = "";
        this.currencyCode = "";
        this.urlPath = "";
        this.value = 0;
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(String name, String currencyCode) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = this.getCoinbaseUrl();
        this.value = this.getApiValue();
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(String name, String currencyCode, String urlPath) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = urlPath;
        this.value = this.getApiValue();
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(String name, String currencyCode, double value, String urlPath) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.value = value;
        this.urlPath = urlPath;
        this.lastUpdated = LocalDateTime.now();
    }
    public Currency(String name, String currencyCode, String urlPath, double value, LocalDateTime lastUpdated) {
        this.name = name;
        this.currencyCode = currencyCode;
        this.urlPath = urlPath;
        this.value = value;
        this.lastUpdated = lastUpdated;
    }
    //=============================-Methods-==================================

    public String getCoinbaseUrl() {
        return "https://api.coinbase.com/v2/prices/%s-USD/spot".formatted(this.currencyCode);
    }
    //----------------------------Update-Value--------------------------------
    public void updateValue() {
        this.lastUpdated = LocalDateTime.now();
        this.value = this.getApiValue();
    }
    //---------------------------Get-Api-Value--------------------------------
    public double getApiValue() {
        String currencyApiJson = this.getCurrencyApiJson();
        boolean isEmpty = currencyApiJson == null || currencyApiJson.isEmpty();
        boolean isNoDataString = currencyApiJson.equals(ApiDataRetriever.NO_DATA_ERROR_MESSAGE);
        if (isNoDataString || isEmpty) {
            String errorMessage = "No data received from API for %s. Aborting Currency construction.".formatted(this.currencyCode);
            throw new IllegalStateException(errorMessage);
        }
        return this.getValueFromJson(currencyApiJson);
    }
    //----------------------------Format-Value--------------------------------
    public String formatValue(double value) {
        String reformattedValue = decimalFormat.format(value);
        if (reformattedValue == null) {
            reformattedValue = "";
        }
        return reformattedValue;
    }
    //-----------------------Get-Currency-Api-Json----------------------------
    public String getCurrencyApiJson() {
        ApiDataRetriever apiDataRetriever = new ApiDataRetriever(this.urlPath);
        return apiDataRetriever.getResponse();
    }
    //------------------------Get-Value-From-Json-----------------------------
    public double getValueFromJson(String json) {
        StringBuilder currencyJson = new StringBuilder(json);
        String amountKey = "amount\":\"";
        int amountLength = amountKey.length();
        int indexOfAmount = currencyJson.indexOf(amountKey) + amountLength;
        int endIndex = currencyJson.length();
        currencyJson.delete(0, indexOfAmount);
        int indexOfCloseQuote = currencyJson.indexOf("\"");
        currencyJson.delete(indexOfCloseQuote, endIndex);
        String currencyValueString = currencyJson.toString();
        double currencyValue = Double.parseDouble(currencyValueString);
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

    public static Currency fromExisting(String currencyCode) {
        Set<Currency> currencies = SupportedCurrencies.SUPPORTED_CURRENCIES;
        return currencies.stream()
            .filter(currency -> currency.getCurrencyCode().equalsIgnoreCase(currencyCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Currency with code " + currencyCode + " not found."));
    }

    public static Currency fromHistory(CurrencyHistory currencyHistory) {
        return Currency.builder()
            .name(currencyHistory.getName())
            .currencyCode(currencyHistory.getCurrency().getCurrencyCode())
            .urlPath(currencyHistory.getCurrency().getUrlPath())
            .value(currencyHistory.getValue())
            .build();
    }

    public static Currency from(Currency currency) {
        Currency newCurrency = new Currency(currency.getName(), currency.getCurrencyCode(),
            currency.getUrlPath(), currency.getValue(),
            currency.getLastUpdated());
        return newCurrency;
    }
    //============================-Overrides-=================================

    //------------------------------Equals------------------------------------
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Currency comparedCurrency) {
            boolean sameName = this.name.equals(comparedCurrency.name);
            boolean sameCode = this.currencyCode.equals(comparedCurrency.currencyCode);
            boolean sameValue = this.value == comparedCurrency.value;
            boolean sameUrl = this.urlPath.equals(comparedCurrency.urlPath);
            boolean sameLastUpdated = this.lastUpdated.equals(comparedCurrency.lastUpdated);
            return sameName && sameCode && sameValue && sameUrl && sameLastUpdated;
        }
        return false;
    }
    //------------------------------Hash-Code---------------------------------

    //------------------------------To-String---------------------------------
    @Override
    public String toString() {
        String currencyString = """
                %18s --- %5s - %16s""".formatted(this.name, this.currencyCode,
            "$" + decimalFormat.format(this.value));
        return currencyString;
    }
    public static CurrencyBuilder builder() {
        return new CurrencyBuilder();
    }
    //=============================-Getters-==================================
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }
    //=============================-Setters-==================================
    public void setValue(double value) {
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return this.currencyCode;
    }

    @Override
    public void setId(String id) {
        this.currencyCode = id;
    }
}
