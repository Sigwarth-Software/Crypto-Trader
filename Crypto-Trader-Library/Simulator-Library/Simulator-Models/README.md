# Simulator-Models

Simulator-Models defines the simulation concepts shared across the simulator's communication and processing layers. It gives historical portfolio experiments a consistent vocabulary.

## What it enables

The module describes simulation results in relation to a user, a portfolio or asset, a period, and a financial outcome. It also defines the time scales used to summarize detailed output, from minute-level views through monthly views.

These concepts let different simulation features agree on what is being examined and how results should be organized over time.

## Place in the platform

Simulation requests and services use this shared vocabulary while other simulator modules perform replay and deliver results. Some concepts remain preliminary, and defining result information does not by itself provide a saved simulation history. The current service workflow computes results for responses, with dedicated result persistence still to be developed.
