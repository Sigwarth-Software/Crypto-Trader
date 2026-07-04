CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trade_event_portfolio_time_id
    ON trade_events (portfolio_id, trade_time DESC, id DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trade_event_asset_history
    ON trade_events (portfolio_asset_history_id);