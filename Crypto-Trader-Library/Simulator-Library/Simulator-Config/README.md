# Simulator-Config

Simulator-Config connects incoming simulation messages to the platform's historical simulation capability. It provides the application wiring that turns a portfolio request into a summary or detailed result.

## What it enables

A simulation host can accept both overall portfolio comparison requests and requests for a timeline of portfolio values. Each request is passed to the appropriate processing service and its response is returned through the configured messaging path.

This connection lets simulation processing operate within the same request and response conventions as other event-driven platform features.

## Operational context

The host supplies messaging destinations, market-data access, and the services needed to perform the replay. Processing failures currently produce zero-valued fallback responses, with an empty timeline for detailed requests. Those fallbacks do not carry a separate failure indicator, so logs are needed to distinguish processing errors from otherwise plausible results.
