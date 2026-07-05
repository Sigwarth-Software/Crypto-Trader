CREATE INDEX CONCURRENTLY market_snapshots_last_updated_desc_idx
    ON market_snapshots (last_updated DESC)
    INCLUDE (id);

CLUSTER market_snapshots USING market_snapshots_last_updated_desc_idx;