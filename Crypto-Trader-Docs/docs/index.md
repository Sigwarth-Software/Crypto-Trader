# Crypto Trader

A modular, end‑to‑end platform for building, testing, and running crypto
trading systems. This is a project spearheaded by Sigwarth Software. Crypto
Trader is the leading the way a non-profit organization with the goal of
democratizing high frequency trading.

This documentation site brings together interactive APIs, module references,
and library docs so you can explore and integrate quickly.

> Tip: Use the navigation bar at the top to jump to the Crypto-Trader and Data
> API explorers, module documentation, or shared library docs.

---

## At a glance

- **Crypto-Trader API** - orchestration and application-facing endpoints. See: [Crypto-Trader API](api/index.md)
- **Crypto-Trader-Data API** - market data ingestion, storage, and access. See: [Data API](data/index.md)
- **Modules** - purposes and reference links under [Module references](#module-references)
- **Libraries** - reusable packages used across services under [Library](library/index.md)

---

## Quickstart

### Explore APIs in the browser

- Open [Crypto-Trader API](api/index.md) or [Data API](data/index.md) from the top navigation.
- Explore Engine, Contact, and other services through the [module references](#module-references).
- Use the interactive explorer to list endpoints, inspect schemas, and try requests.

> Note: Some endpoints require authentication. See the [Api module README](reference/Api/README.md) for context.

---

## Navigation guide

- Getting started guides: Guides → [Guides](guides/index.md)
- API explorers:
  - [Crypto-Trader API](api/index.md)
  - [Crypto-Trader-Data API](data/index.md)

### Module references

The repository contains the following 23 top-level modules. These modules all
hold separate responsibilities and offload work to other modules where
relevant. This helps load balance and allocate resources for services as well
as promote a decentralized architecture for reliability.

| Module | Purpose | Documentation |
| --- | --- | --- |
| Admin | Desktop operations and administration interface. | [README](reference/Admin/README.md) · [Complete Docs](reference/Admin/api/index.html) |
| Agent | MCP server exposing platform tools to AI assistants. | [README](reference/Agent/README.md) · [Complete Docs](reference/Agent/api/index.html) |
| Analysis | Market analytics, model training, and prediction workflows. | [README](reference/Analysis/README.md) |
| Api | Application backend for identity, accounts, portfolios, and orchestration. | [README](reference/Api/README.md) · [Complete Docs](reference/Api/api/index.html) |
| Assets | Shared resources and asset access for platform interfaces. | [README](reference/Assets/README.md) · [Complete Docs](reference/Assets/api/index.html) |
| Chat | Conversational interface using AI and the Agent module's tools. | [README](reference/Chat/README.md) · [Complete Docs](reference/Chat/api/index.html) |
| Console | Command-oriented interaction with the platform. | [README](reference/Console/README.md) · [Complete Docs](reference/Console/api/index.html) |
| Contact | Messaging and support communications. | [README](reference/Contact/README.md) · [Complete Docs](reference/Contact/api/index.html) |
| Coverage | Aggregated test coverage reports across applications and libraries. | [README](reference/Coverage/README.md) · [Coverage report](coverage/index.html) |
| Data | Market data collection, price history, and data exchange. | [README](reference/Data/README.md) · [Complete Docs](reference/Data/api/index.html) |
| Development | Project management and organization. | [README](reference/Development/README.md) |
| Docs | Guides, API specifications, and the assembled documentation site. | [README](reference/Docs/README.md) |
| Engine | Trading execution and strategy orchestration. | [README](reference/Engine/README.md) · [Complete Docs](reference/Engine/api/index.html) |
| Health | Service availability and dependency health checks. | [README](reference/Health/README.md) · [Complete Docs](reference/Health/api/index.html) |
| Library | Shared domain concepts, service foundations, and reusable interface components. | [README](library/module.md) · [Library overview](library/index.md) |
| Logging | Structured application logs and operational context. | [README](reference/Logging/README.md) · [Complete Docs](reference/Logging/api/index.html) |
| Mobile | Native mobile interface for platform users. | [README](reference/Mobile/README.md) |
| Security | Control monitoring, request safeguards, and security signals. | [README](reference/Security/README.md) · [Complete Docs](reference/Security/api/index.html) |
| Simulator | Historical portfolio replay and strategy comparisons. | [README](reference/Simulator/README.md) · [Complete Docs](reference/Simulator/api/index.html) |
| Testing | Shared testing support, fixtures, and verification conventions. | [README](reference/Testing/README.md) |
| Transactions | Trade order and transaction-record domain. | [README](reference/Transactions/README.md) |
| Version | Version metadata and release tooling. | [README](reference/Version/README.md) · [Complete Docs](reference/Version/api/index.html) |
| Website | Web interface and public platform information. | [README](reference/Website/README.md) · [Complete Docs](reference/Website/api/index.html) |

### Shared libraries

The [Library overview](library/index.md) introduces the reusable packages that
support these modules. The Library navigation contains their individual references,
including the following families:

- [Admin](library/Admin-Library/README.md), [Agent](library/Agent-Library/README.md), [Api](library/Api-Library/README.md), and [Assets](library/Assets-Library/README.md).
- [Chat](library/Chat-Library/README.md), [Console](library/Console-Library/README.md), [Contact](library/Contact-Library/README.md), and [Data](library/Data-Library/README.md).
- [Desktop](library/Desktop-Library/README.md), [Development](library/Development-Library/README.md), [Engine](library/Engine-Library/README.md), and [Externals](library/Externals-Library/README.md).
- [Health](library/Health-Library/README.md), [Logging](library/Logging-Library/README.md), [Security](library/Security-Library/README.md), and [Simulator](library/Simulator-Library/README.md).
- [Testing](library/Testing-Library/README.md), [Universal](library/Universal-Library/README.md), and [Version](library/Version-Library/README.md).

---

## Contributing & support

- Issues and feature requests: use GitHub Issues in the main repository.
- See CHANGELOG.md in the repo root for release notes.
- For security‑related topics, consult the Security module under References
  and follow responsible disclosure guidelines.

Next steps: Head to the [Guides](guides/index.md) or open an API explorer (e.g., [Crypto Trader API](api/index.md)) to start building.
