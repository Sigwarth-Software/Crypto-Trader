# Universal-Communication

Universal-Communication provides a shared way to exchange values that change over time. It supports the timestamped measurements used across market history, comparisons, and portfolio visualizations.

## What it enables

Services can return a numeric value with its timestamp, optionally attach a name, or pair it with a comparison value. These common shapes make it easier for different features to feed charts and comparisons without introducing a different representation for every use case.

For example, a simulation can return the strategy's value alongside a baseline at each point in time, while a history feature can return a single series of market values.

## Place in the platform

This module sits at the communication boundary between features. It gives producers and consumers an agreed shape for the data while leaving calculation, storage, and presentation to their respective layers. Participants still need to agree on timestamp conventions and the meaning of each value.
