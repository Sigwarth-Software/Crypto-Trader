# Console-Infrastructure

Console-Infrastructure supplies shared descriptive metadata for the Crypto Trader console. It supports a consistent way to explain available commands and what they do.

## What it enables

Command authors can associate a command name with a human-readable description. Console features can use that information to make capabilities discoverable and help users understand an action before choosing it.

This shared convention helps command help stay connected to the feature it describes as the console grows.

## Place in the platform

The module supplies the descriptive foundation. Other console modules discover commands, assemble help sections, format the resulting text, and perform the requested actions. Its current scope is command-help metadata; command registration and execution are handled elsewhere.
