# Agent-Repositories

Agent-Repositories provides storage access for records of agent tool executions. It is part of the persistence foundation behind the platform's agent accountability features.

## What it enables

Execution records can be retained and retrieved so other parts of the platform can inspect agent activity. Keeping that access in a dedicated module allows the record format and the services that use it to evolve around a clear persistence boundary.

The current module supports general record operations; specialized audit searches and reporting behavior are outside its present scope.

## Place in the platform

Agent-Models owns the record definitions, and Agent-Services exposes their management to the service layer. The application provides database connectivity and decides how agent actions become recorded events. This module supplies access to stored evidence rather than the policy governing which actions must be recorded.
