# Assets-Config

Assets-Config establishes how an application delivers packaged static assets and controls access around them. It supports the HTTP resource delivery needed by Crypto Trader's interfaces.

## What it enables

Resources packaged with an application can be made available through a common asset URL space. A cache policy allows clients to reuse those resources for a period, reducing repeated downloads.

The configuration makes asset delivery and health checks publicly accessible while requiring authentication for other requests covered by its security policy. This allows basic resource consumption and availability checks without granting unrestricted access to the wider application.

## Place in the platform

The host supplies the resource content, identity configuration, and application environment. This module establishes delivery and access behavior rather than producing frontend bundles or maintaining an asset catalog. When combined with other application security policies, the overall access arrangement needs to account for how those policies interact.
