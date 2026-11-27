# Simulator-Scripts

Simulator-Scripts provides the calculation layer for replaying portfolio behavior through historical prices. It enables simulation features to explore a strategy's effect on holdings and portfolio value over time.

## What it enables

Starting shares and cash can be evaluated as market prices progress through a historical period. The replay follows the platform's simulation trading context and can produce both a final state and a sequence of values for charting.

The module also supports a buy-and-hold comparison, where starting cash is valued as an initial purchase and the resulting holdings are carried through the same history. Together, these calculations give callers a basis for comparing active simulated behavior with a simpler baseline.

## Place in the platform

Simulator-Services supplies historical data and coordinates the broader request. This module performs the in-memory replay and valuation work. History ordering and scenario isolation matter because replay follows the supplied sequence and changes the simulated holdings as it progresses.
