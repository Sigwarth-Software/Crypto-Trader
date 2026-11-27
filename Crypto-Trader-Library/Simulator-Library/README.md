# Simulator-Library

Simulator-Library groups the capabilities used to explore portfolio behavior against historical market data. It supports comparing the platform's simulated trading behavior with a buy-and-hold baseline over a selected period.

## What it enables

Portfolio holdings and starting cash can be replayed against stored currency history to examine how their value changes. Summary comparisons provide an overall result, while detailed time series make it possible to visualize the path taken through the period.

The library family brings together the simulation vocabulary, request and response information, replay calculations, orchestration, and messaging integration. Its modules allow a host application to assemble the parts needed for a simulation workflow.

## Interpretation and scope

Results depend on the available history, sampling, and implemented trading rules. They describe historical scenarios rather than expected future returns. The current workflow primarily computes results for callers; the dedicated simulation repository module remains reserved for future persistence work.
