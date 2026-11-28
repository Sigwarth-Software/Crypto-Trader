# Simulator-Repositories

Simulator-Repositories is the reserved persistence area for the Simulator library family. Its intended role is to support storing and retrieving simulation results when a dedicated result-history capability is introduced.

## Intended purpose

Persisted simulation outcomes could provide a basis for revisiting historical runs and comparing experiments over time. A separate persistence boundary would keep those records distinct from the market history used as simulation input.

## Current scope

This module does not yet provide simulation record storage. The current simulation services read market history through the Data layer and compute results for their callers. Simulation result concepts exist in Simulator-Models, but a saved-results workflow is not supplied here yet.

The module therefore marks an architectural responsibility rather than an available result-history feature.
