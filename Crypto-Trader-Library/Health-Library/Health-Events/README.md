# Health-Events

Health-Events enables services to request and exchange health status through the platform's messaging infrastructure. It gives application health a shared communication path across service boundaries.

## What it enables

A caller can request health information and receive the response associated with that request. This supports health-related coordination in workflows that already use messaging rather than relying only on direct HTTP access.

Shared request and response conventions make the exchange consistent with the platform's other event-driven features.

## Place in the platform

Health-Communication defines the information exchanged, and Universal-Events supplies correlation and waiting behavior. The host application connects the exchange to its destinations and the service that determines health status. This module provides the messaging side of that interaction; HTTP health endpoints and the checks themselves belong to their respective application layers.
