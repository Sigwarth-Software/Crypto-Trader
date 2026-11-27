# Simulator-Events

Simulator-Events provides the messaging connection between simulation callers and the services that perform historical replay. It allows portfolio simulation to participate in the platform's event-driven workflows.

## What it enables

Applications can request a summary or detailed simulation through messaging and wait for the corresponding result. The exchange allows more time for processing than the shared default request window, reflecting that a historical replay can involve substantial data and calculation.

The module also supports publishing simulation requests for downstream processing. It establishes consistent communication names for simulation-related exchanges so callers and processing applications can agree on the flow.

## Place in the platform

Simulator-Communication defines the scenario and result information. Simulator-Config connects incoming portfolio requests to the processing services, and Universal-Events supplies correlation behavior. Applications still need destination mappings and response routing. Some exchange names reserve room for asset-level workflows beyond the portfolio consumers currently supplied.
