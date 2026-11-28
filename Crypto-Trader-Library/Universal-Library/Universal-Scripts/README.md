# Universal-Scripts

Universal-Scripts supplies small shared utilities for interpreting time and request context across Crypto Trader. These capabilities help different features handle common supporting information consistently.

## What it enables

Time expressed as epoch milliseconds can be presented as local date and time. Request handling and logging can also identify an originating address from forwarded request information or the direct connection address.

These utilities support application behavior such as timestamp interpretation and contextual logging without tying that behavior to a particular trading feature.

## Integration context

Local time conversion follows the host's time zone, so deployments should use a deliberate time convention when comparing or storing results. Forwarded addresses reflect information supplied through the request path; their trustworthiness depends on the host's proxy configuration. The module provides shared interpretation helpers, while applications remain responsible for those environment choices.
