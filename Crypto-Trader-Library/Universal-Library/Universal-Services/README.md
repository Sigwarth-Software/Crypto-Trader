# Universal-Services

Universal-Services provides a consistent foundation for working with stored business entities. It gives domain services a shared approach to finding, creating, updating, and removing records.

## What it enables

Features can use common behavior for identity lookup, existence checks, batch operations, and persistence. This reduces differences in how modules handle routine record operations and gives domain services a foundation on which to add their own rules.

The module also supports consistent operational logging, including masking fields designated as sensitive. That makes persistence activity easier to understand while allowing record owners to control which information appears in logs.

## Place in the platform

Domain-specific services, including agent execution record services, use this foundation with their own entities and repositories. The host supplies database access and transaction management. Business validation and decisions about when records should be changed remain with the domain that owns them.
