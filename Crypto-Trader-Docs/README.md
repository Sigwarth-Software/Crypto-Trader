# Crypto Trader — Docs Module
## Your fastest path from idea to live trading, and more

---

Crypto‑Trader‑Docs is the hub that makes Crypto Trader transparent, learnable,
and fast to build on. It brings together step‑by‑step guides, live API
references (generated from running services), and per‑module references so
newcomers and contributors can move from zero to paper trading in minutes—and
experts can ship confidently at scale.

Important: Past results and backtests do not guarantee future performance.
Always start in paper mode. Liability is your own.

## 📚 Documentation
Your fastest path from idea to live trading is in our docs. Explore
step-by-step guides, live API references, and full module documentation.

- Start here: Guides — onboarding, connecting an exchange, paper trading, and
  going live.
    - [Crypto Trader Home](https://sigwarth-software.github.io/Crypto-Trader/)
    - [Guides index](https://sigwarth-software.github.io/Crypto-Trader/guides/)
- APIs you can build on:
    - [Crypto Trader API](https://sigwarth-software.github.io/Crypto-Trader/api/)
    - [Data API (market and data services)](https://sigwarth-software.github.io/Crypto-Trader/data/)
    - [Engine API (strategy execution)](https://sigwarth-software.github.io/Crypto-Trader/engine/)
- Deep dives and references (per module):
    - Api • Admin • Assets • Data • Engine • Library • Logging • Testing •
      Version
    - [Reference hub](https://sigwarth-software.github.io/Crypto-Trader/reference/)

Quick start (docs):
1. Read the Getting Started guide and run in paper mode.
2. Connect an exchange with read-only or small limits.
3. Pick a plan (Free/Pro/Elite), set guardrails, and monitor results.

Tip: Always begin in paper mode and set strict limits before funding.

## ⭐️ What this module is
- The documentation system for Crypto Trader, powering the public site and
  internal contributor experience.
- A generator that assembles live API specs (OpenAPI), Kotlin/Java references
  (Dokka), and hand‑authored guides (MkDocs Material).
- A single source of truth for links, navigation, and module reference docs.

## ✅ Why it matters
- Transparency: live, versioned documentation builds trust with users.
- Speed: clear guides and auto‑generated references reduce back‑and‑forth and
  onboarding time.
- Accuracy: API pages are generated from running services, reducing drift.
- Consistency: per‑module READMEs and references keep the platform aligned.

## 🔗 Where it fits in the platform
- Publishes the docs site at: `https://sigwarth-software.github.io/Crypto-Trader/`.
- Pulls in artifacts from other modules:
  - OpenAPI specs by starting Api/Data/Engine in a special `docs` profile.
  - Per‑module references built with Dokka.
- Acts as the discoverability layer for Guides, APIs, and References.

## 🏗️ How the docs are built
The GitHub Actions workflow `.github/workflows/docs.yml` orchestrates the
entire build and publish pipeline:

1. Build all modules with Maven to ensure code is fresh.
2. Generate Dokka HTML references for all modules.
3. Start Api, Data, and Engine with the `docs` Spring profile to expose
   OpenAPI docs on local ports, then fetch their `v3/api-docs.yaml`.
4. Build this docs module with Maven to collect and copy artifacts into the
   docs tree.
5. Build the MkDocs Material site and deploy to GitHub Pages.

This keeps API references live and synchronized with the codebase on each push
to `main`.


## 🗺️ Site structure highlights
Defined in `mkdocs.yml`:
- Home and Guides
- API sections: CT API, Data API, Engine API
- References for each module (README + full Dokka HTML): Api, Admin, Assets,
  Data, Engine, Library, Logging, Testing, Version
- Material theme with light/dark palettes and features like sticky tabs and
  code copy buttons

## 📝 Conventions
- Prefer short, scannable guides with clear prerequisites and fenced code
  blocks for commands and code samples.
- Keep links absolute when pointing to the published site; use relative paths
  for intra‑repo references.
- Update per‑module READMEs alongside code changes to keep the reference
  section trustworthy.
- Use backticks around inline code, commands, and file paths (e.g.,
  `mkdocs.yml`, `mvn`).

## ❓ Questions or help
Email Oliver Lear Sigwarth (@theoliverlear): `sigwarthsoftware@gmail.com`

## 📄 License
See `LICENSE.md` in the repository root.
