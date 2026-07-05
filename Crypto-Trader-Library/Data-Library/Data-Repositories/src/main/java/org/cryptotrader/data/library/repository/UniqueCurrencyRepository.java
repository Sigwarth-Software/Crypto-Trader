package org.cryptotrader.data.library.repository;

import org.cryptotrader.data.library.entity.currency.UniqueCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniqueCurrencyRepository extends JpaRepository<UniqueCurrency, String> {
    boolean existsByCurrency(String currencyCode);
}
