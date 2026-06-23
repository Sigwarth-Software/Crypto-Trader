// TODO: Move to an assets file.
const HTTP_PROTOCOL_PATTERN = /^https?:\/\//i
const WEBSOCKET_PROTOCOL_PATTERN = /^wss?:\/\//i

function browserOrigin(): string {
    if (typeof window !== 'undefined' && typeof window.location?.origin === 'string') {
        return window.location.origin
    }
    return 'http://localhost'
}

function browserWebSocketOrigin(): string {
    if (typeof window !== 'undefined' && typeof window.location?.host === 'string') {
        const websocketProtocol: string =
            window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        return `${websocketProtocol}//${window.location.host}`
    }
    return 'ws://localhost'
}

export function resolveAbsoluteHttpUrl(url: string): string {
    if (HTTP_PROTOCOL_PATTERN.test(url)) {
        return url
    }
    return new URL(url, browserOrigin()).toString()
}

export function resolveWebSocketUrl(url: string): string {
    if (WEBSOCKET_PROTOCOL_PATTERN.test(url)) {
        return url
    }
    if (HTTP_PROTOCOL_PATTERN.test(url)) {
        const absoluteHttpUrl: URL = new URL(url)
        absoluteHttpUrl.protocol = absoluteHttpUrl.protocol === 'https:' ? 'wss:' : 'ws:'
        return absoluteHttpUrl.toString()
    }
    return new URL(url, browserWebSocketOrigin()).toString()
}
