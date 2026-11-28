# Scripts Directory

This folder historically contained helper scripts for ad‑hoc operational tasks.

### Available Scripts

- `generate-coverage.main.kts`: Generates coverage documentation using `.aws/buildspec-docs.yml` steps 8–10. Run `kotlin scripts/generate-coverage.main.kts` from the repository root to check aggregate module coverage, run Maven tests with the CI coverage profile, generate ReportGenerator HTML/charts/badges, refresh `Crypto-Trader-Docs/docs/coverage/index.html`, and rebuild MkDocs. Requires Kotlin, JDK 23+, Maven, Python, .NET, ReportGenerator (`dotnet tool install --global dotnet-reportgenerator-globaltool`), MkDocs and Material on PATH, plus CI-equivalent test configuration. Use `--skip-tests` to reuse an existing aggregate `jacoco.xml`, `--skip-site-build` when previewing with `mkdocs serve`, or `--refresh-dependencies` to force Maven updates. For the reported `/Crypto-Trader/coverage/index.html` 404, generate coverage before running the link checker; when serving the static site, rebuild it (the default). This script does not start application services or Docker containers.
- `generate-openapi.main.kts`: Builds and starts API/Data with the docs profile, exports both OpenAPI specs, runs the Docs Maven verification, and stops its service processes. Run `kotlin scripts/generate-openapi.main.kts` from the repository root; use `--skip-build` to reuse existing JARs or `--help` for prerequisites and output paths. Requires JDK 23, Maven, Docker, and CI-equivalent application configuration/AWS access. Kafka is left running.
- `generateJwtToken.kts`: Generates a secure random Base64-encoded string for JWT secrets.
- `repository-log-space-stats.kts`: Reports total file space used by files under `logs` directories, with extension, module, and logs-directory breakdowns.
- `setup-data-https.kts`: Run on the Data computer to generate a certificate for its private LAN IPv4 address and export the public CA certificate that Engine must trust.
- `setup-local-https.main.kts`: Generates shared trusted local certificates under repo-level `certs/` with `mkcert` and prints API, Data, Analysis, and Website HTTPS startup steps.
- `verify-maven-publish.main.kts`: Benchmarks Maven publish pipeline bottlenecks by validating GitHub Packages endpoint resolution against a personal access token parameter using HTTP GET and measuring standalone JVM startup overhead.
