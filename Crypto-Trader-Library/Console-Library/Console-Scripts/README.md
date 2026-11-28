# Console-Scripts

Console-Scripts turns structured command descriptions into readable console help. It makes the console's available actions easier to scan and understand.

## What it enables

Commands and their descriptions can be presented in aligned sections, with related subcommands visually grouped underneath them. Consistent formatting helps users find an action and understand its purpose without needing to inspect its implementation.

When no help information is available, the module provides a clear message instead of an empty display.

## Place in the platform

The module works with help information supplied by the console's models and discovery features. It prepares the text that an output channel can display. Gathering command information and carrying out commands remain responsibilities of the wider console system.
