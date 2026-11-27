#!/usr/bin/env kotlin

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.collections.emptySet
import kotlin.system.exitProcess

val localUrlString = "http://127.0.0.1:8001/Crypto-Trader/"
val localUrl: URI = URI.create(localUrlString)
val originallyVisited: MutableSet<String> = mutableSetOf()
val visited: MutableSet<String> = mutableSetOf()
val broken: MutableSet<String> = mutableSetOf()

fun getClient(urlString: String): HttpURLConnection {
    val url: URL = URI.create(urlString).toURL()
    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("User-Agent", "Mozilla/5.0")
    connection.connectTimeout = 10_000
    connection.readTimeout = 10_000
    return connection
}

fun getUrlsFromHtml(html: String, urlString: String): Set<String> {
    /*
     * 1. Check the anchor block
     * 2. Create a block
     * 3. Check for a non-closing character
     * 4. Break the block
     * 5. Look for the href
     * 6. Capture after equals
     */
    val anchorRegex = Regex("""<a\b[^>]*?\bhref\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    var urls: MutableList<String> = anchorRegex.findAll(html)
        .map { it.groupValues[1] }
        .toMutableList()

    urls = urls.filter {
        val notSeen: Boolean = !visited.contains(it) && !broken.contains(it)
        val validStart: Boolean = !it.startsWith("mailto:") && !it.startsWith("#") && !it.startsWith("javascript:") && !it.startsWith(".")
        val containsInvalidChars: Boolean = it.contains(Regex("""\[|\]"""))
        val isHttp: Boolean = it.startsWith("http") || it.startsWith("https")
        val isLocal: Boolean = it.startsWith(localUrlString)

        if (isHttp && !isLocal) {
            return@filter false
        }
        (notSeen && validStart && !containsInvalidChars) || isLocal
    }.toMutableList()
    val absoluteUrls: List<String> = urls.map {
        val uri: URI = URI.create(urlString)
        val resolvedUrl: URI = uri.resolve(it)
        resolvedUrl.toString()
    }
    return absoluteUrls.toSet()
}

fun getUrls(client: HttpURLConnection, visitedSet: MutableSet<String>): Set<String> {
    val urlString: String = client.url.toString()
    val html: String = try {
        if (client.responseCode == HttpURLConnection.HTTP_OK) {
            visitedSet.add(urlString)
            client.inputStream.bufferedReader().use {
                it.readText()
            }
        } else {
            broken.add(urlString)
            throw RuntimeException("Response code ${client.responseCode} for ${client.url}")
        }
    } catch (exception: Exception) {
        broken.add(urlString)
        System.err.println("Broken link: ${client.url} (${exception.message})")
        return emptySet()
    } finally {
        client.disconnect()
    }
    return getUrlsFromHtml(html, urlString)
}

fun getAllUrls(urlString: String): Set<String> {
    if (visited.contains(urlString)) {
        return emptySet()
    }
    val client: HttpURLConnection = getClient(urlString)
    val urls: Set<String> = getUrls(client, visited)
    val furtherUrls: MutableSet<String> = mutableSetOf()

    for (url in urls) {
        if (visited.contains(url)) {
            continue
        }
        println("Visiting $url...")
        furtherUrls.addAll(getAllUrls(url))
    }
    return furtherUrls
}

fun main() {
    val client: HttpURLConnection = getClient(localUrlString)
    val urls: Set<String> = getUrls(client, originallyVisited)
    val furtherUrls: MutableSet<String> = mutableSetOf()
    for (url in urls) {
        furtherUrls.addAll(getAllUrls(url))
    }

    println("Checked ${visited.size + broken.size} URLs, ${broken.size} broken.")
    if (broken.isNotEmpty()) {
        exitProcess(1)
    }
}

main()
