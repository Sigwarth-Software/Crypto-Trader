import logging
from datetime import datetime

from attr import attr
from attrs import define

from src.crypto_trader_analysis.apps.news.models.analysis.sentiment.sentiment_analyzer import SentimentAnalyzer
from src.crypto_trader_analysis.apps.news.models.analysis.sentiment.sentiment_result import SentimentResult
from src.crypto_trader_analysis.apps.news.models.article.article import Article
from src.crypto_trader_analysis.core.service_client import post_json

@define
class ScoredArticle(Article):
    sentiment_result: SentimentResult = attr(default=None)
    last_updated: datetime = attr(default=None)
    
    def score_article(self, send_to_server: bool = True) -> None:
        sentiment_analyzer: SentimentAnalyzer = SentimentAnalyzer()
        self.sentiment_result = sentiment_analyzer.get_sentiment(self.text)
        self.last_updated = datetime.now()
        if send_to_server:
            self.send_to_server()
        
    def to_json(self):
        return {
            "articleId": self.article_id,
            "title": self.title,
            "publishDate": self.publish_date,
            "source": self.source,
            "url": self.url,
            "positiveScore": self.sentiment_result.positive_score,
            "neutralScore": self.sentiment_result.neutral_score,
            "negativeScore": self.sentiment_result.negative_score,
            "compositeScore": self.sentiment_result.composite_score,
            "cryptoRelevance": self.sentiment_result.crypto_relevance,
            "lastUpdated": self.last_updated.utcnow().strftime("%Y-%m-%dT%H:%M:%S"),
        }
    
    def send_to_server(self):
        if self.sentiment_result is None:
            self.score_article()
        payload: dict = self.to_json()
        try:
            from django.conf import settings
            response = post_json(settings.CT_DATA_BASE_URL, "/data/news-sentiment/add", payload)
            if response.status_code == 200:
                logging.info("Article sent successfully.")
            else:
                print(f"Error sending article: CODE - {response.status_code}")
        except Exception as e:
            print(f"Error sending article: {e}")
            
