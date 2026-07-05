@file:JvmName("GatewayAliases")
package org.cryptotrader.universal.library.events.alias

import java.util.concurrent.CompletableFuture

typealias GatewayResponses<Response> = MutableMap<String, CompletableFuture<Response>>