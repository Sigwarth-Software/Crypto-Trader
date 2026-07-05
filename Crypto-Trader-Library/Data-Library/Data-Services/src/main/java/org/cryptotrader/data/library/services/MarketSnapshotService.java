package org.cryptotrader.data.library.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.data.library.entity.currency.Currency;
import org.cryptotrader.data.library.services.models.MarketSnapshotOperations;
import org.cryptotrader.universal.library.model.annotation.TimeTracked;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSnapshotService implements MarketSnapshotOperations {
    //============================-Constants-=================================
    private static final String[] DEFAULT_COLUMNS = { "last_updated" };
    //============================-Variables-=================================
    private final JdbcTemplate jdbcTemplate;

    private final Set<String> knownColumns = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void initKnownColumns() {
        try {
            final String sql = "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(table_name) = 'MARKET_SNAPSHOTS'";
            final List<String> existing = this.jdbcTemplate.queryForList(sql, String.class);
            if (existing != null) {
                this.knownColumns.addAll(existing);
            }
        } catch (DataAccessException exception) {
            log.warn("Could not load known columns (table may not exist yet): {}", exception.getMessage());
        }
    }

    //=============================-Methods-==================================

    //---------------------------Save-Snapshot--------------------------------
    @TimeTracked(expectedMillis = 5, shouldPersist = true)
    @Transactional
    public void saveSnapshot(Map<String, Currency> currencies) {
        if (!isValidCurrencyMap(currencies)) {
            log.warn("No valid currencies provided for a market snapshot.");
            return;
        }
        List<String> columns = new ArrayList<>(List.of(DEFAULT_COLUMNS));
        List<Object> params = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("America/Chicago"));
        params.add(currentTime);
        loadColumnsAndParams(currencies, columns, params);
        String columnList = String.join(", ", columns);
        String questionMarks = getQuestionMarks(columns);
        this.executeSnapshotQuery(currencies, columnList, questionMarks, params);
    }
    //-----------------------Execute-Snapshot-Query---------------------------
    private void executeSnapshotQuery(Map<String, Currency> currencies,
                                      String columnList,
                                      String questionMarks,
                                      List<Object> params) {
        String query = """
                INSERT INTO market_snapshots (%s) VALUES (%s)"""
                .formatted(columnList, questionMarks);
        try {
            this.jdbcTemplate.update(query, params.toArray());
            log.debug("Inserted market snapshot with {} currencies.", currencies.size());
        } catch (DataAccessException exception) {
            log.error("Failed to insert market snapshot: ", exception);
            throw exception;
        }
    }
    //-------------------------Get-Question-Marks-----------------------------
    private static String getQuestionMarks(List<String> columns) {
        return columns.stream().map(currency -> "?").collect(Collectors.joining(", "));
    }
    //----------------------Load-Columns-And-Params---------------------------
    private void loadColumnsAndParams(Map<String, Currency> currencies,
                                      List<String> columns,
                                      List<Object> params) {
        currencies.forEach((code, currency) -> {
            String priceColumn = toPriceColumn(code);

            if (!this.knownColumns.contains(priceColumn)) {
                createCurrencyColumn(priceColumn);
                this.knownColumns.add(priceColumn);
            }

            columns.add(priceColumn);
            params.add(currency.getValue());
        });
    }
    //-----------------------Create-Currency-Column---------------------------
    private void createCurrencyColumn(String priceColumn) {
        this.jdbcTemplate.execute("""
            ALTER TABLE market_snapshots
            ADD COLUMN IF NOT EXISTS %s NUMERIC(34,18)
        """.formatted(priceColumn));
    }
    //--------------------------To-Price-Column-------------------------------
    private static String toPriceColumn(String code) {
        return code.toLowerCase() + "_price";
    }
    //-----------------------Is-Valid-Currency-Map----------------------------
    private static boolean isValidCurrencyMap(Map<String, Currency> currencies) {
        return !(currencies == null || currencies.isEmpty());
    }
}