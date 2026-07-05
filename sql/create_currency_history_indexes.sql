CREATE INDEX currency_history_code_ts_desc_inc_val
    ON currency_history USING btree (currency_code, last_updated DESC)
    INCLUDE (currency_value);


CREATE INDEX currency_history_ts_desc_code_inc_val
    ON currency_history (last_updated DESC, currency_code)
    INCLUDE (currency_value);


CREATE INDEX currency_history_btc_ts_desc_inc_val
    ON currency_history (last_updated DESC)
    INCLUDE (currency_value)
    WHERE currency_code = 'BTC';

CREATE INDEX brin_currency_history_ts
    ON currency_history USING brin (last_updated)
    WITH (pages_per_range = 128);
