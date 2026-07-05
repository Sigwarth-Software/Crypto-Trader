package org.cryptotrader.data.library.repository;
//=================================-Imports-==================================

import org.cryptotrader.data.library.entity.currency.Currency;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CurrencyRepository extends JpaRepository<Currency, String> {
    //============================-Methods-===================================

    //-------------------Get-Currency-By-Currency-Code------------------------
    Currency getCurrencyByCurrencyCode(String currencyCode);
    //------------------------Get-Currency-By-Name----------------------------
    Currency getCurrencyByName(String currencyName);

    boolean existsByCurrencyCode(String currencyCode);

    @Query("SELECT c.currencyCode FROM Currency c ORDER BY c.currencyCode ASC")
    List<String> findAllCurrencyCodes();

    List<Currency> findTop10ByOrderByValueDesc();
    @Query("SELECT c FROM Currency c" +
            " WHERE c.name NOT LIKE '%Wrapped%' " +
            "AND c.name NOT LIKE '%wrapped %' " +
            "AND c.name NOT LIKE '%Staked%' " +
            "AND c.name NOT LIKE '%staked %' " +
            "ORDER BY c.value DESC")
    List<Currency> findTopTenNonEncapsulated();

    @Query("SELECT c FROM Currency c" +
            " WHERE c.name NOT LIKE '%Wrapped%' " +
            "AND c.name NOT LIKE '%wrapped %' " +
            "AND c.name NOT LIKE '%Staked%' " +
            "AND c.name NOT LIKE '%staked %' " +
            "ORDER BY c.value DESC")
    List<Currency> findNonEncapsulated(Pageable pageable);
}
