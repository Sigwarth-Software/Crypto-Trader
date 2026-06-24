import json
import time
from datetime import datetime, timezone, timedelta, date
import os
from typing import Any
from django.conf import settings

import worldnewsapi
from worldnewsapi import SearchNews200Response, ApiResponse

from src.crypto_trader_analysis.apps.news.models.sources.news_sources import ALL_NEWS_SOURCES_LINKS, \
    get_by_link, FILTERED_NEWS_SOURCES_LINKS

api_key: str = settings.WORLDNEWS_API_KEY
api_config = worldnewsapi.Configuration(api_key={'apiKey': api_key})
api_instance = worldnewsapi.NewsApi(worldnewsapi.ApiClient(api_config))
# TODO: Optimize and maximize the number of queries. Max 100 chars.
QUERIES = [
    "(crypto OR cryptocurrency OR bitcoin OR btc OR eth OR coinbase OR solana OR stablecoin OR altcoin)"
    # "(crypto OR cryptocurrency OR bitcoin OR btc OR coinbase)",
    # "(ethereum OR eth OR blockchain OR web3 OR defi OR stablecoin)",
    # "(xrp OR solana OR cardano OR bnb OR dogecoin OR litecoin or crypto mining)"
]
SLEEP_DURATION = 0.6
PAGE_SIZE = 100
NEWS_SOURCES_LIMIT = 10
QUOTA_STOP_THRESHOLD = 5.0

def chunk_sources(source_links: list[str]) -> list[str]:
    return [
        ','.join(source_links[index:index + NEWS_SOURCES_LIMIT])
        for index in range(0, len(source_links), NEWS_SOURCES_LIMIT)
    ]

def get_source_groups(include_forbes: bool) -> list[str]:
    source_links: list[str] = ALL_NEWS_SOURCES_LINKS if include_forbes else FILTERED_NEWS_SOURCES_LINKS
    return chunk_sources(source_links)

def get_time_bounds(start_date: date, end_date: date) -> tuple[str, str, str]:
    day_label: str = start_date.isoformat()
    end_day_label: str = end_date.isoformat()
    earliest_date: str = f"{day_label} 00:00:00"
    latest_date: str = f"{end_day_label} 23:59:59"
    return day_label, earliest_date, latest_date

def day_bounds_strings(day_offset: int) -> tuple[str, str, str]:
    now_utc: datetime = datetime.now(timezone.utc)
    target: datetime = now_utc - timedelta(days=day_offset)
    day_label: str = target.date().isoformat()
    earliest_date: str = f"{day_label} 00:00:00"
    latest_date: str = f"{day_label} 23:59:59"
    return day_label, earliest_date, latest_date

def get_points_remaining(headers: Any) -> float:
    headers_dict: dict = dict(headers)
    print(headers_dict)
    return float(headers_dict.get("X-API-Quota-Left"))


def get_points_used(headers: Any) -> float:
    headers_dict: dict = dict(headers)
    return float(headers_dict.get("X-API-Quota-Request"))

def fetch_articles(earliest_date: str,
                   latest_date: str,
                   num_articles: int = 50,
                   include_forbes: bool = True):
    total_points_used: float = 0.0
    collected: dict[str, dict] = {}
    source_groups: list[str] = get_source_groups(include_forbes)
    for query in QUERIES:
        for news_sources in source_groups:
            offset: int = 0
            while len(collected) < num_articles:
                source_count: int = len(news_sources.split(','))
                if source_count > NEWS_SOURCES_LIMIT:
                    raise ValueError(f"Too many news sources in request batch: {source_count}")
                api_response: ApiResponse[SearchNews200Response] = api_instance.search_news_with_http_info(
                    language="en",
                    source_country="us",
                    news_sources=news_sources,
                    number=PAGE_SIZE,
                    categories="business,technology,politics,science",
                    sort="publish-time",
                    sort_direction="desc",
                    text=query,
                    earliest_publish_date=earliest_date,
                    latest_publish_date=latest_date,
                    offset=offset,
                )
                remaining_points: float = get_points_remaining(api_response.headers)
                points_used: float = get_points_used(api_response.headers)
                total_points_used += points_used
                print(f"""
                Used: {points_used:.4f} points
                Remaining: {remaining_points:.4f} points
                """)
                data: dict = api_response.data.to_dict()
                news_items: list = data.get("news", []) or []
                print(f"Returned {len(news_items)} news items for {source_count} sources")
                if not news_items:
                    break
                for news_item in news_items:
                    print(news_item)
                    url = news_item.get("url")
                    if not url or url in collected:
                        continue
                    author: str = news_item.get("author")
                    # author: str = get_author(news_item, url)
                    collected[url] = {
                        "id": news_item.get("id"),
                        "title": news_item.get("title"),
                        "url": url,
                        "publish_date": news_item.get("publish_date"),
                        "author": author,
                        # "summary": news_item.get("summary"),
                        # "language": news_item.get("language"),
                        # "source_country": news_item.get("source_country"),
                        # "sentiment_api": news_item.get("sentiment"),
                        "text": news_item.get("text"),
                        "keywords": query,
                        "source": get_by_link(url).name if get_by_link(url) else "Unknown",
                    }
                    if len(collected) >= num_articles:
                        break
                if remaining_points < QUOTA_STOP_THRESHOLD:
                    return list(collected.values())
                returned: int = len(news_items)
                if returned < PAGE_SIZE:
                    break
                offset += returned
                rate_limit_sleep()
            if len(collected) >= num_articles:
                break
            rate_limit_sleep()
        if len(collected) >= num_articles:
            break
        rate_limit_sleep()

    print(f"""Used {total_points_used:.4f} points
    Total articles: {len(collected)}""")
    return list(collected.values())

def load_to_json(days_articles: list) -> None:
    timestamp = datetime.now(timezone.utc).strftime("%Y_%m_%d__%H_%M_%S")
    out_path = f"data/temp/temp_data_{timestamp}.json"
    with open(out_path, "w", encoding="utf-8") as file:
        json.dump(days_articles, file, ensure_ascii=False, indent=2)
    print(f"\nSaved {sum(len(day['articles']) for day in days_articles)} articles across {len(days_articles)} days → {out_path}")

def rate_limit_sleep():
    time.sleep(SLEEP_DURATION)

def get_by_target(num_articles: int, start_date: date, end_date: date, include_forbes: bool = True) -> list[dict]:
    day_label, earliest_str, latest_str = get_time_bounds(start_date, end_date)
    print(f"\n=== Collecting for {day_label} (UTC {earliest_str}..{latest_str}) ===")
    items = fetch_articles(earliest_str, latest_str, num_articles=num_articles, include_forbes=include_forbes)
    print(f"Collected {len(items)} articles for {day_label}")
    all_days: list[dict] = [{
        "day": day_label,
        "earliest_publish_date": earliest_str,
        "latest_publish_date": latest_str,
        "articles": items,
    }]
    return all_days

def get_by_day(num_days: int, days_offset: int, num_articles: int, include_forbes: bool = True) -> list[dict]:
    all_days = []
    for day_offset in range(num_days):
        day_label, earliest_str, latest_str = day_bounds_strings(day_offset + days_offset)
        print(f"\n=== Collecting for {day_label} (UTC {earliest_str}..{latest_str}) ===")
        items = fetch_articles(earliest_str, latest_str, num_articles=num_articles, include_forbes=include_forbes)
        print(f"Collected {len(items)} articles for {day_label}")
        all_days.append({
            "day": day_label,
            "earliest_publish_date": earliest_str,
            "latest_publish_date": latest_str,
            "articles": items,
        })
    return all_days

# TODO: Make this OOP. Function chaining doesn't do enough to define a type
#       flow.
if __name__ == "__main__":
    articles = get_by_day(2, 350, 5)
    load_to_json(articles)
