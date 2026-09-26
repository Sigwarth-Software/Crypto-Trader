package org.cryptotrader.data.library.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketSnapshotsBackfiller {
    //============================-Constants-=================================
    private static final String BASE_CODE = "BTC";
    private static final String BASE_COLUMN = "btc_price";
    private static final double WINDOW_SECONDS = 2.5;
    private static final int BATCH_SIZE = 10_000;
    private static final double NANO_TO_SECONDS = 1_000_000_000.0;
    //============================-Variables-=================================
    private final JdbcTemplate jdbc;
    //=============================-Methods-==================================

    //--------------------------Build-Snapshots-------------------------------
    public void buildSnapshots(final boolean fullRefresh) {
        final List<String> codes = this.getCurrencyCodes();
        if (codes.isEmpty()) {
            log.warn("No non-base currencies found – nothing to do.");
            return;
        }
        long baseRows = this.getBaseRows();
        if (baseRows == 0) {
            log.warn("No base currency rows found – nothing to back-fill.");
            return;
        }
        this.executeSnapshotCreation(fullRefresh, codes, baseRows);
    }
    //---------------------Execute-Snapshot-Creation--------------------------
    private void executeSnapshotCreation(final boolean fullRefresh,
                                         final List<String> codes,
                                         final long baseRows) {
        this.initTable();
        this.createBaseColumn();
        this.addCodeColumns(codes);
        if (fullRefresh) {
            this.truncateTable();
        }
        final int cores = Math.min(Runtime.getRuntime().availableProcessors(), 18);
        final ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        final ScheduledExecutorService rowTicker = Executors.newSingleThreadScheduledExecutor();
        final LongAdder numInserted = new LongAdder();
        final long startedTimeNanos = System.nanoTime();

        runRowTracker(rowTicker, numInserted, startedTimeNanos);
        this.runInsertThreads(baseRows, threadPool, codes, numInserted);
        awaitShutdown(threadPool, rowTicker);
        logBackfillCompletion(startedTimeNanos, numInserted, cores);
    }
    //---------------------------Await-Shutdown-------------------------------
    private static void awaitShutdown(final ExecutorService pool,
                                      final ScheduledExecutorService ticker) {
        pool.shutdown();
        try {
            final boolean result = pool.awaitTermination(365, TimeUnit.DAYS);
            log.info("Thread pool shutdown completed: {}", result);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        ticker.shutdownNow();
    }
    //---------------------------Truncate-Table-------------------------------
    private void truncateTable() {
        this.jdbc.execute("TRUNCATE TABLE market_snapshots;");
    }
    //--------------------------Add-Code-Columns------------------------------
    private void addCodeColumns(final List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            log.warn("No currency codes provided for column addition.");
            return;
        }
        codes.forEach(code -> this.jdbc.execute(addCodeColumn(code)));
    }
    //--------------------------Add-Code-Column-------------------------------
    private static String addCodeColumn(String code) {
        return """
                    ALTER TABLE market_snapshots
                    ADD COLUMN IF NOT EXISTS %s NUMERIC(34,18);
                """.formatted(toPriceColumn(code));
    }
    //-------------------------Create-Base-Column-----------------------------
    private void createBaseColumn() {
        this.jdbc.execute("""
        ALTER TABLE market_snapshots
        ADD COLUMN IF NOT EXISTS %s NUMERIC(34,18);
    """.formatted(BASE_COLUMN));
    }
    //----------------------Log-Backfill-Completion---------------------------
    private static void logBackfillCompletion(final long startedTimeNanos,
                                              final LongAdder inserted,
                                              final int cores) {
        final long nanosElapsed = System.nanoTime() - startedTimeNanos;
        final double sec = nanosElapsed / NANO_TO_SECONDS;
        final double rowsPerSecond = inserted.sum() / Math.max(sec, 0.001);
        log.info("Snapshot back-fill complete – {} rows, {} rows/s ({} threads).",
                inserted.sum(),
                rowsPerSecond,
                cores);
    }
    //-----------------------------Init-Table---------------------------------
    private void initTable() {
        this.jdbc.execute("""
        CREATE TABLE IF NOT EXISTS market_snapshots(
            id           BIGSERIAL    PRIMARY KEY,
            last_updated TIMESTAMPTZ UNIQUE
        );
    """);
    }
    //---------------------------Get-Base-Rows--------------------------------
    private Long getBaseRows() {
        String baseQuery = """
                SELECT COUNT(*) FROM currency_history
                WHERE  currency_code = ?""";
        return this.jdbc.queryForObject(baseQuery, Long.class, BASE_CODE);
    }
    //-------------------------Get-Currency-Codes-----------------------------
    private List<String> getCurrencyCodes() {
        String selectQuery = "SELECT DISTINCT currency_code " +
                             "FROM currencies WHERE currency_code <> ? " +
                             "ORDER BY 1";
        return this.jdbc.queryForList(selectQuery, String.class, BASE_CODE);
    }
    //-------------------------Run-Insert-Threads-----------------------------
    private void runInsertThreads(long baseRows,
                                  ExecutorService threadPool,
                                  List<String> codes,
                                  LongAdder numInserted) {
        long batchCount = (baseRows + BATCH_SIZE - 1) / BATCH_SIZE;
        LongStream.range(0, batchCount)
                .parallel()
                .forEach(slice -> threadPool.submit(() -> {
                    int offset = (int) (slice * BATCH_SIZE);
                    int rowsProcessed = this.processBatchSlice(offset, codes);
                    numInserted.add(rowsProcessed);
                }));
    }
    //--------------------------Run-Row-Tracker-------------------------------
    private static void runRowTracker(final ScheduledExecutorService scheduler,
                                      final LongAdder inserted,
                                      final long bootTimeNanos) {
        scheduler.scheduleAtFixedRate(new Runnable() {
            long lastCount = 0;
            @Override
            public void run() {
                final long currentRowSum = inserted.sum();
                final long numRowsProcessed = currentRowSum - lastCount;
                lastCount = currentRowSum;
                final double elapsedSec = (System.nanoTime() - bootTimeNanos) / NANO_TO_SECONDS;
                final double averageRowsPerSecond = elapsedSec == 0 ? 0 : currentRowSum / elapsedSec;
                final long roundedAverageRowsPerSecond = Math.round(averageRowsPerSecond);
                log.info("{} rows/s   |   {} rows/s avg   |   {} total",
                        numRowsProcessed,
                        roundedAverageRowsPerSecond,
                        currentRowSum);

            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    //------------------------Process-Batch-Slice-----------------------------
    private int processBatchSlice(int offset, List<String> codes) {
        String sql = getSnapshotFillQuery(offset, codes);
        return this.jdbc.update(sql);
    }
    //----------------------Get-Snapshot-Fill-Query---------------------------
    private static String getSnapshotFillQuery(int offset, List<String> codes) {
        String insertCols = getInsertColumns(codes);
        String selectCols = getCoalesceQueries(codes);
        String lateralJoins = getLateralJoins(codes);
        return getInsertQuery(offset, insertCols, selectCols, lateralJoins);
    }
    //--------------------------Get-Insert-Query------------------------------
    private static String getInsertQuery(int offset,
                                         String insertCols,
                                         String selectCols,
                                         String lateralJoins) {
        return """
                INSERT INTO market_snapshots (%s)
                WITH btc_data AS (
                    SELECT last_updated,
                           currency_value
                    FROM   currency_history
                    WHERE  currency_code = '%s'
                    ORDER  BY last_updated ASC
                    LIMIT  %d
                    OFFSET %d
                )
                SELECT
                    btc.last_updated,
                    %s
                FROM   btc_data btc
                %s
                ORDER BY btc.last_updated ASC
                ON CONFLICT (last_updated) DO NOTHING;
                """.formatted(insertCols, BASE_CODE, BATCH_SIZE, offset,
                              selectCols, lateralJoins);
    }
    //-------------------------Get-Lateral-Joins------------------------------
    private static String getLateralJoins(@NotNull final List<String> codes) {
        return codes.stream()
                    .map(MarketSnapshotsBackfiller::getLateralJoin)
                    .collect(Collectors.joining("\n"));
    }
    //--------------------------Get-Lateral-Join------------------------------
    private static String getLateralJoin(@NotNull final String code) {
        return """
                LEFT JOIN LATERAL (
                    SELECT currency_value
                    FROM   currency_history
                    WHERE  currency_code = '%s'
                      AND  last_updated BETWEEN btc.last_updated - INTERVAL '%s seconds'
                                            AND btc.last_updated + INTERVAL '%s seconds'
                    ORDER  BY last_updated DESC
                    LIMIT  1
                ) %s ON TRUE
                """.formatted(code,
                              WINDOW_SECONDS,
                              WINDOW_SECONDS,
                              code.toLowerCase());
    }
    //------------------------Get-Coalesce-Queries----------------------------
    private static String getCoalesceQueries(final List<String> codes) {
        final String baseAlias = "btc.currency_value AS " + BASE_COLUMN + ",\n   ";
        return baseAlias + codes.stream()
                                .map(MarketSnapshotsBackfiller::getCoalesceQuery)
                                .collect(Collectors.joining(",\n  "));
    }
    //-------------------------Get-Coalesce-Query-----------------------------
    private static String getCoalesceQuery(String code) {
        return "COALESCE(%s.currency_value, NULL) AS %s".formatted(code.toLowerCase(),
                                                                   toPriceColumn(code));
    }
    //-------------------------Get-Insert-Columns-----------------------------
    private static String getInsertColumns(List<String> codes) {
        final String insertBase = "last_updated, " + BASE_COLUMN + ", ";
        return insertBase + codes.stream()
                                 .map(MarketSnapshotsBackfiller::toPriceColumn)
                                 .collect(Collectors.joining(", "));
    }
    //-------------------------Normalize-Entries------------------------------
    private void normalizeEntries() {
        this.removeCloseEntries();
        this.updateEntryIds();
    }
    //------------------------Remove-Close-Entries----------------------------
    private void removeCloseEntries() {
        this.jdbc.execute("""
            BEGIN;

            WITH ordered AS (
                SELECT id,
                       last_updated,
                       lag(last_updated) OVER (ORDER BY last_updated) AS prev_time
                FROM   market_snapshots
            )
            DELETE FROM market_snapshots ms
                USING  ordered ord
            WHERE  ms.id = ord.id
              AND  ord.prev_time IS NOT NULL
              AND  ord.last_updated - ord.prev_time < INTERVAL '3.5 seconds';

            COMMIT;
        """);
    }
    //--------------------------Update-Entry-Ids------------------------------
    private void updateEntryIds() {
        this.jdbc.execute("""
            BEGIN;

            ALTER TABLE market_snapshots DROP CONSTRAINT market_snapshots_pkey;
            ALTER TABLE market_snapshots ALTER COLUMN id DROP NOT NULL;

            UPDATE market_snapshots SET id = NULL;

            WITH ordered AS (
                SELECT ctid,
                       row_number() OVER (ORDER BY last_updated) AS new_id
                FROM   market_snapshots
            )
            UPDATE market_snapshots ms
            SET    id = ord.new_id
            FROM   ordered ord
            WHERE  ms.ctid = ord.ctid;

            ALTER TABLE market_snapshots ALTER COLUMN id SET NOT NULL;
            ALTER TABLE market_snapshots ADD PRIMARY KEY (id);

            SELECT setval('market_snapshots_id_seq',
                          (SELECT max(id) FROM market_snapshots), true);

            COMMIT;
        """);
    }
    //--------------------------To-Price-Column-------------------------------
    private static @NotNull String toPriceColumn(final String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or blank.");
        }
        return code.toLowerCase() + "_price";
    }
}