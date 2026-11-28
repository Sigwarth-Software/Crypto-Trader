# syntax=docker/dockerfile:1
FROM node:22-bookworm-slim AS node
FROM mcr.microsoft.com/dotnet/sdk:8.0-bookworm-slim AS dotnet

# Build tools only. Compose mounts the repository at runtime so generated
# references and the finished site remain available on the host.
FROM maven:3.9.9-eclipse-temurin-23 AS builder
ARG KOTLIN_VERSION=2.2.20
ARG REPORTGENERATOR_VERSION=5.4.18
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash ca-certificates curl unzip python3 python3-venv libicu-dev libssl-dev libatomic1 zlib1g \
    && rm -rf /var/lib/apt/lists/*
COPY --from=node /usr/local/ /opt/node/
COPY --from=dotnet /usr/share/dotnet/ /usr/share/dotnet/
ENV DOTNET_ROOT=/usr/share/dotnet
ENV PATH="/opt/kotlinc/bin:/opt/node/bin:/opt/mkdocs/bin:/usr/share/dotnet:/opt/reportgenerator:${PATH}"
ENV DOTNET_CLI_TELEMETRY_OPTOUT=1
RUN curl --fail --location --retry 3 \
        "https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip" \
        --output /tmp/kotlin.zip \
    && unzip -q /tmp/kotlin.zip -d /opt \
    && rm /tmp/kotlin.zip \
    && python3 -m venv /opt/mkdocs \
    && /opt/mkdocs/bin/pip install --no-cache-dir mkdocs mkdocs-material \
    && dotnet tool install dotnet-reportgenerator-globaltool \
        --version "${REPORTGENERATOR_VERSION}" --tool-path /opt/reportgenerator
WORKDIR /app
ENTRYPOINT ["kotlin", "scripts/generate-docs.main.kts"]
CMD ["--external-kafka"]

# MkDocs performs its initial build before starting the live HTTP server.
FROM python:3.12-slim AS preview
WORKDIR /docs
RUN pip install --no-cache-dir mkdocs==1.6.1 mkdocs-material==9.8.2
COPY Crypto-Trader-Docs/mkdocs.yml ./mkdocs.yml
COPY Crypto-Trader-Docs/docs/ ./docs/
EXPOSE 8000
CMD ["mkdocs", "serve", "--dev-addr", "0.0.0.0:8000"]
