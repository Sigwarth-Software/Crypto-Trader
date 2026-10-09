# database.py
import io
import logging
import tempfile
import time
import traceback
from django.conf import settings

import pandas as pd
from attr import attr
from attrs import define
from sqlalchemy import create_engine, QueuePool, text, Engine, \
    PoolProxiedConnection

from src.crypto_trader_analysis.apps.learning.models.database.query_type import QueryType
from src.crypto_trader_analysis.apps.learning.models.currency_json_generator import get_all_currency_codes


@define
class Database:
    #=============================-Variables-=================================
    user: str = attr(factory=lambda: settings.DATABASES['default']['USER'])
    password: str = attr(factory=lambda: settings.DATABASES['default']['PASSWORD'])
    name: str = attr(factory=lambda: settings.DATABASES['default']['NAME'])
    port: str = attr(factory=lambda: settings.DATABASES['default']['PORT'])
    host: str = attr(factory=lambda: settings.DATABASES['default']['HOST'])
    connection = attr(default=None)
    engine: Engine = attr(default=None)

    #---------------------------Attrs-Post-Init-------------------------------
    def __attrs_post_init__(self):
        logging.debug("Connecting to Crypto Trader's database...")
        connection_url: str = self.get_connection_url()
        self.engine: Engine = (
            create_engine(
                connection_url,
                poolclass=QueuePool,
                pool_size=20,
                max_overflow=40,
                echo=False
            )
        )

    #-------------------------Get-Connection-Url------------------------------
    def get_connection_url(self) -> str:
        return f"postgresql+psycopg2://{self.user}:{self.password}@{self.host}:{self.port}/{self.name}"

    #------------------------Get-Inaccurate-Models----------------------------
    def get_inaccurate_models(self, error_threshold: float,
                              only_recent_predictions: bool = False) -> list[str]:
        # TODO: Refactor to allow for num rows for recent results.
        recent_query: str = ''
        if only_recent_predictions:
            recent_query = """
                AND last_updated > (NOW() - INTERVAL '1 hour')
            """
        query = f"""
            SELECT
                currency_code,
                AVG(percent_difference) AS avg_difference
            FROM
                predictions
            WHERE
                percent_difference > %(error_threshold)s
            {recent_query}
            GROUP BY
                currency_code
            ORDER BY
                avg_difference DESC;
        """
        params = {"error_threshold": error_threshold}
        try:
            logging.debug(f"Executing query...")
            df = pd.read_sql_query(query, self.engine, params=params)
            logging.debug(f"Query executed successfully.")
        except Exception as exception:
            logging.error(f"Error executing query for inaccurate models: {str(exception)}")
            df = pd.DataFrame()
        return [currency_code for currency_code in df['currency_code'].tolist()]

    #-------------------------Get-Currencies-List-----------------------------
    @staticmethod
    def get_currencies_list() -> list[str]:
        return get_all_currency_codes(True)

    #------------------------Fetch-Data-In-Batches----------------------------
    def fetch_data_in_batches(self,
                              batch_size: int = 10000,
                              target_currency: str = 'BTC',
                              limit: int = 20000,
                              query_type: QueryType = QueryType.HISTORICAL_PRICE) -> list[pd.DataFrame]:
        batch_size = max(batch_size, 10_000)
        dataframe: pd.DataFrame = self.fetch_data(target_currency, limit, query_type)
        sliced_dataframes: list[pd.DataFrame] = []
        if len(dataframe) == 0:
            return [pd.DataFrame()]
        for i in range(0, len(dataframe), batch_size):
            batch_df = dataframe.iloc[i:i + batch_size]
            sliced_dataframes.append(batch_df)
        logging.info(f"Fetched {len(dataframe)} rows in {len(sliced_dataframes)} batches.")
        return sliced_dataframes

    #-----------------------------Fetch-Data----------------------------------
    def fetch_data(self,
                   target_currency: str = 'BTC',
                   limit=20000,
                   query_type: QueryType = QueryType.HISTORICAL_PRICE,
                   rows_per_log: int = 10_000,
                   in_bulk: bool = True) -> pd.DataFrame:
        logging.info(f"Fetching {target_currency} data from database.")
        params, query = self.build_query(limit, query_type, target_currency)
        next_mark: int = 0
        start_time: float = time.time()
        try:
            logging.debug(f"Beginning query execution limited to {limit} rows.")
            return self.execute_query(next_mark, params, query,
                                      rows_per_log, start_time, limit,
                                      rows_per_log, in_bulk)
        except Exception as exception:
            traceback.print_exc()
            return pd.DataFrame()

    #--------------------------Los-Row-Progress-------------------------------
    def _log_row_progress(self,
                          rows_seen: int,
                          start_time: float,
                          next_mark: int,
                          rows_per_log: int,
                          total_rows: int) -> int:
        while rows_seen >= next_mark:
            elapsed: float = time.time() - start_time or 1e-9
            rows_per_second: float = rows_seen / elapsed
            estimated_total_seconds: float = (total_rows - rows_seen) / rows_per_second
            estimated_formated_str: str = time.strftime("%H:%M:%S", time.gmtime(estimated_total_seconds))
            logging.info("Retrieved %d rows (%.1f rows/sec) | %s remaining...",
                         next_mark,
                         rows_per_second,
                         estimated_formated_str)
            next_mark += rows_per_log
        return next_mark


    #---------------------------Execute-In-Bulk-------------------------------
    def _execute_in_bulk(self, params, query: str) -> pd.DataFrame:
        try:
            bound_params = {
                key: value
                for key, value in params.items()
                if f":{key}" in query
            }
            compiled_query = (
                text(query)
                .bindparams(**bound_params)
                .compile(
                    dialect=self.engine.dialect,
                    compile_kwargs={"literal_binds": True},
                )
            )
            return self._execute_copy_to_dataframe(str(compiled_query))
        except Exception as exception:
            logging.error(f"Error executing in bulk: {str(exception)}")
            raise

    #----------------------Execute-Copy-To-Dataframe--------------------------
    def _execute_copy_to_dataframe(self, compiled_sql: str) -> pd.DataFrame:
        raw_connection: PoolProxiedConnection = self.engine.raw_connection()
        try:
            with raw_connection.cursor() as cursor:
                # Set transaction to read-only for the session
                cursor.execute("SET TRANSACTION ISOLATION LEVEL READ COMMITTED READ ONLY")
                cursor.execute("SET statement_timeout = '600s'")
                formatted_query: str = compiled_sql.replace(';', '')
                copy_command: str = f"COPY ({formatted_query}) TO STDOUT WITH (FORMAT CSV, HEADER)"
                # 20 GB in memory before spilling to disk
                with tempfile.SpooledTemporaryFile(
                    max_size=20 * 1024 * 1024 * 1024,
                    mode='w+b'
                ) as buffer:
                    cursor.copy_expert(copy_command, buffer)
                    buffer.seek(0)
                    try:
                        return pd.read_csv(buffer, engine='c')
                    except pd.errors.ParserError:
                        logging.warning("C parser failed, retrying with Python engine")
                        buffer.seek(0)
                        return pd.read_csv(buffer, engine='python')
        except Exception as exception:
            logging.error(f"Error executing copy to dataframe: {str(exception)}")
            logging.debug(f"Compiled SQL: {compiled_sql}")
            raise
        finally:
            raw_connection.close()

    #----------------------------Execute-Query--------------------------------
    def execute_query(self,
                      next_mark: int,
                      params, query: str,
                      rows_per_log: int,
                      start_time: float,
                      limit: int = 10000,
                      batch_size = 10000,
                      in_bulk: bool = True) -> pd.DataFrame:
        if in_bulk:
            return self._execute_in_bulk(params, query)
        all_frames: list[pd.DataFrame] = []
        streamed_rows: int = 0
        with self.engine.connect().execution_options(stream_results=True) as connection:
            connection.execute(text("SET TRANSACTION ISOLATION LEVEL READ COMMITTED READ ONLY"))
            for chunk in pd.read_sql_query(
                    text(query),
                    connection,
                    params=params,
                    chunksize=batch_size):
                all_frames.append(chunk)
                streamed_rows += len(chunk)
                next_mark = self._log_row_progress(
                    streamed_rows,
                    start_time,
                    next_mark,
                    rows_per_log,
                    limit)
                if streamed_rows >= next_mark:
                    logging.info("Retrieved %d rows...", streamed_rows)
                    next_mark += rows_per_log
        return pd.concat(all_frames, ignore_index=True)

    #-----------------------Get-Price-Column-Names----------------------------
    def _get_price_column_names(self) -> list[str]:
        return [f"{code.lower()}_price" for code in self.get_currencies_list()]

    #-------------------------Get-Select-Columns------------------------------
    def _get_select_columns(self) -> str:
        price_cols: list[str] = self._get_price_column_names()
        select_cols: list[str] = ["last_updated", *price_cols]
        return ", ".join(select_cols)

    #----------------------Get-Market-Snapshot-Query--------------------------
    def _get_market_snapshot_query(self) -> str:
        select_list: str = self._get_select_columns()
        return f"""
            SELECT {select_list}
            FROM   market_snapshots
            ORDER  BY last_updated DESC
            LIMIT  :limit
        """

    #------------------Get-Market-Snapshot-Spaced-Query-----------------------
    def _get_market_snapshot_spaced_query(self) -> str:
        select_list = self._get_select_columns()
        return f"""
            WITH bounds AS (
                SELECT MIN(id) AS min_id,
                       MAX(id) AS max_id
                FROM   market_snapshots
            ),
            params AS (
                SELECT
                    GREATEST(
                        ((max_id - min_id + 1 + :limit - 1) / :limit), 1
                    )::bigint AS step,
                    min_id,
                    max_id
                FROM bounds
            ),
            ids AS (
                SELECT
                    generate_series(max_id, min_id, -step) AS id
                FROM params
                LIMIT :limit
            )
            SELECT {select_list}
            FROM   market_snapshots ms
            JOIN   ids USING (id)
            ORDER  BY ms.last_updated DESC;
        """

    #-----------------------------Build-Query---------------------------------
    def build_query(self, limit: int, query_type: QueryType, target_currency: str) -> tuple[dict, str]:
        query: str = ""
        params: dict = {"target_currency": target_currency}
        if query_type == QueryType.CURRENT_PRICE:
            logging.info(f"Fetching current price for {target_currency}.")
            query = f"SELECT * FROM currencies WHERE currency_code = :target_currency;"
        elif query_type == QueryType.HISTORICAL_PRICE:
            logging.info(f"Fetching historical prices limited to {limit} rows.")
            query = self._get_market_snapshot_query()
            logging.debug("Fetched history query.")
            params = {"target_currency": target_currency, "limit": limit}
        elif query_type == QueryType.HISTORICAL_PRICE_SPACED:
            logging.info(f"Fetching spaced historical price data limited to {limit} rows.")
            query = self._get_market_snapshot_spaced_query()
            params = {"target_currency": target_currency, "limit": limit}
        return params, query

    #--------------------------------Close------------------------------------
    def close(self) -> None:
        if self.engine is not None:
            self.engine.dispose()