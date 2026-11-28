# Simulator-Services

Simulator-Services coordinates historical portfolio simulations from the starting scenario through the final comparison. It connects market history with the portfolio replay needed to answer a simulation request.

## What it enables

A caller can select assets, starting holdings, cash, and a time period, then receive a comparison between simulated trading and a buy-and-hold baseline. Detailed results add a timeline suitable for charts, with time-based summarization to make longer periods easier to explore.

The service brings together starting market values, available historical prices, the applicable sampling interval, and simulation calculations. This gives callers a cohesive simulation capability rather than requiring them to assemble those steps themselves.

## Understanding results

The current summary represents final portfolio values, which should be distinguished from net gains when presenting the outcome. Results also depend on historical coverage and alignment between asset series. The service computes responses using the host's market-data and database facilities; it does not currently maintain a saved simulation history.
