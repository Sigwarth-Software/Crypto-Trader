package org.cryptotrader.data.library.entity.currency;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cryptotrader.universal.library.entity.Identifiable;
import org.cryptotrader.universal.library.model.annotation.Loggable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "unique_currencies")
@AllArgsConstructor
@NoArgsConstructor
public class UniqueCurrency extends Identifiable<String> {
    @Id
    @Column(name = "currency_code")
    private String currency;

    @Loggable
    @Column(name = "currency_name")
    private String name;

    @Transient
    private String urlPath;

    @Loggable
    @Column(name = "currency_value", columnDefinition = "DECIMAL(34, 18)")
    private double value;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Transient
    private Currency associatedCurrency;

    public UniqueCurrency(Currency currency) {
        this.name = currency.getName();
        this.currency = currency.getCurrencyCode();
        this.urlPath = currency.getUrlPath();
        this.value = currency.getValue();
        this.lastUpdated = LocalDateTime.now();
        this.associatedCurrency = currency;
    }

    @Override
    public String getId() {
        return this.currency;
    }

    @Override
    public void setId(String id) {
        this.currency = id;
    }
}
