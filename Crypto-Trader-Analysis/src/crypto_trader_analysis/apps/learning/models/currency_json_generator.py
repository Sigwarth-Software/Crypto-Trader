import json
import logging
from pathlib import Path
from typing import Any

import requests
from requests import Response

_CURRENCIES_JSON = Path(__file__).resolve().parents[4] / "src" / "main" / "resources" / "static" / "currencies.json"


def fetch_json(url):
    response: Response = requests.get(url)
    return response.json()

def get_currencies():
    currencies = fetch_json("https://api.exchange.coinbase.com/currencies")
    exchange_rates = fetch_json("https://api.coinbase.com/v2/exchange-rates?currency=USD")
    rates = exchange_rates.get("data", {}).get("rates", {})
    matched_cryptos: list[dict[str, Any]] = []
    for currency in currencies:
        currency_id = currency.get("id")
        currency_name = currency.get("name")
        currencies_to_skip: list[str] = ["DYP", "USD", "LQTY", "WLUNA", "GUSD",
                                         "DAI", "ME", "MASK", "USDC", "DAR",
                                         "AERGO", "TONE", "RAD", "NU", "DATA"]
        if currency_id in rates:
            currency_contains_numbers: bool = any(char.isdigit() for char in currency_id)
            if currency_id not in currencies_to_skip and not currency_contains_numbers:
                matched_cryptos.append({
                    "name": currency_name,
                    "code": currency_id
                })
    return matched_cryptos

def get_cached_currencies() -> list[dict[str, Any]]:
    with open(_CURRENCIES_JSON, "r") as file:
        cached_currencies = json.load(file)
    return cached_currencies

def save_json(matched_cryptos) -> None:
    output_path: str = "../src/main/resources/static/currencies.json"
    with open(output_path, "w") as file:
        json.dump(matched_cryptos, file, indent=4)
    logging.info(f"Matched cryptocurrencies saved to {output_path}")

def get_all_currency_codes(use_cache= False) -> list[str]:
    if use_cache:
        try:
            currencies = get_cached_currencies()
        except FileNotFoundError:
            logging.debug("Cache file not found. Fetching currencies from API.")
            currencies = get_currencies()
    else:
        currencies = get_currencies()
    currencies_list = [currency["code"] for currency in currencies]
    logging.debug(f"Number of currencies: {len(currencies_list)}")
    return currencies_list

def main():
    save_json(get_currencies())

if __name__ == "__main__":
    main()
