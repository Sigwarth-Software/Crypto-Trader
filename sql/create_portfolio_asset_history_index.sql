CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pah_asset_lastupdated_id
    ON portfolio_asset_history (portfolio_asset_id, last_updated DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pah_portfolio_lastupdated_id
    ON portfolio_asset_history (portfolio_id, last_updated DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pah_asset_lastupdated_id_shares_nz
    ON portfolio_asset_history (portfolio_asset_id, last_updated DESC, id DESC)
    WHERE shares <> 0;
