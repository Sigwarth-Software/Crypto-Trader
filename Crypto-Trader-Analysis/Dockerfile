# Stage 1: Build & Dependencies
FROM python:3.10.16-slim AS builder
WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential libpq-dev && \
    rm -rf /var/lib/apt/lists/*

COPY Crypto-Trader-Analysis/requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.10.16-slim
WORKDIR /app
RUN apt-get update && apt-get install -y libpq5 && rm -rf /var/lib/apt/lists/*
COPY --from=builder /install /usr/local
COPY Crypto-Trader-Analysis/ .

# Run as non-root
RUN useradd -m analysisuser
USER analysisuser

EXPOSE 8000
# Set ANALYSIS_TLS_CERT_FILE and ANALYSIS_TLS_KEY_FILE to serve HTTPS directly.
ENTRYPOINT ["sh", "-c", "gunicorn --pythonpath /app/src --bind ${ANALYSIS_HOST:-0.0.0.0}:${ANALYSIS_PORT:-8000} ${ANALYSIS_TLS_CERT_FILE:+--certfile $ANALYSIS_TLS_CERT_FILE} ${ANALYSIS_TLS_KEY_FILE:+--keyfile $ANALYSIS_TLS_KEY_FILE} crypto_trader_analysis.api.wsgi:application"]
