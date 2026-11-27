# Testing-Infrastructure

Testing-Infrastructure provides shared test conventions and readable test feedback across Crypto Trader. It supports a consistent verification experience as features span multiple modules and services.

## What it enables

Tests can be identified by their scope, including unit, integration, and end-to-end checks. Shared support also helps isolate collaborators during tests and gives contributors a common foundation for preparing verification scenarios.

Hierarchical reporting makes the relationship between test groups and individual outcomes visible. Timing information, captured output, and failure details help contributors locate the cause of a failed run and recognize slower checks.

## Place in the development workflow

The reporting support works with the native JUnit environment, including the IDE, while allowing Maven's reporting path to handle Maven runs. Display preferences can be adjusted for the environment.

This infrastructure improves how tests are organized and understood. Test owners still define assertions, fixtures, external dependencies, and the safeguards needed for checks that interact with running systems.
