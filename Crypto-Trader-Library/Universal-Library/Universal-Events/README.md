# Universal-Events

Universal-Events provides the common messaging foundation that lets Crypto Trader services publish events and exchange requests and responses. It supports cooperation between services whose work travels through the platform's messaging infrastructure.

## What it enables

A service can send a request through a configured message destination and associate the returning response with the original caller. Shared correlation information allows multiple requests to be in progress at once, while time limits prevent callers from waiting indefinitely.

The same foundation supports event publication and the handling of incoming requests. It carries common message metadata, including authorization context where supplied, so domain modules can follow consistent communication conventions.

## Place in the platform

Health, simulation, logging, and other domain messaging modules build on this foundation. Each host application supplies its broker configuration, destinations, and response routing. Pending requests belong to the application instance that sent them, so deployment routing must return responses to that instance. Message content conventions support the platform's encryption integration; this module alone does not perform encryption.
