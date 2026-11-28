# Simulator-Communication

Simulator-Communication defines the information needed to ask for a portfolio simulation and understand its result. It provides a common contract between callers, messaging components, and simulation processing.

## What it enables

A scenario can describe the assets involved, their initial shares and cash, and the historical period to examine. Detailed scenarios can also specify how the output should be summarized over time.

Results carry a comparison between the platform's simulated trading behavior and a baseline. Detailed results add paired values along a timeline, allowing applications to present the comparison as a chart as well as an overall outcome.

## Interpretation

The current service supplies final portfolio values in the summary, so applications should distinguish those from net profit in their presentation. Participants also need consistent local-time conventions and appropriate scenario validation. This module defines the exchanged information; calculation and validation policy belong to the surrounding workflow.
