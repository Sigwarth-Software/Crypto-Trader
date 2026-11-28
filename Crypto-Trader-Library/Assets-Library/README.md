# Assets-Library

Assets-Library groups the shared configuration used to make Crypto Trader's static resources available through an application. It supports the delivery side of resources consumed by the platform's interfaces.

## What it enables

The library family provides a common approach to serving assets over HTTP and applying access rules around that delivery. This helps an asset-serving application expose its packaged resources consistently without every host defining a separate delivery arrangement.

The resource content remains the responsibility of the application and asset modules that supply it.

## Place in the platform

Assets-Config provides the currently implemented web and security configuration. This directory groups that responsibility rather than bundling the resource collection itself. A host uses the concrete configuration module together with the assets it needs to serve.
