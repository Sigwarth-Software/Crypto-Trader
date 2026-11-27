# Engine-Models

Engine-Models is a reserved module within the Engine library family, intended to provide a home for concepts specific to the platform's trading engine.

## Intended purpose

A dedicated engine model layer would allow execution-related concepts to be shared consistently between engine features and their supporting services. It provides an architectural place for that vocabulary as the engine develops.

## Current scope

The module does not yet provide engine-specific models. Existing engine capabilities continue to use definitions available elsewhere in the platform, so this module currently establishes a boundary for future work rather than a usable modeling capability.

Its role should be expanded alongside concrete engine requirements, keeping ownership clear between shared portfolio and trading concepts and concepts that belong specifically to engine execution.
