import { Injectable } from '@angular/core'
import { environment } from '@environments/environment'

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { DpopKeyStoreService } from './dpop-key-store.service'
import { PossibleCryptoKeyPair, PossibleJsonWebKey } from './types'
import {LoggerContext} from "@models/logging/LoggerContext";

/**
 * Manages the client's DPoP key pair using WebCrypto.
 * - Generates a non-extractable private key (ECDSA P-256)
 * - Exports the public key as JWK
 * - Computes RFC7638 JWK thumbprint (jkt)
 * - Signs JWS inputs for DPoP proofs (ES256)
 *
 * Note: Uses ECDSA P-256 and converts DER WebCrypto signatures to JOSE (r|s) format.
 */
@Injectable({
    providedIn: 'root',
})
export class DpopKeyService {
    private keyPair: PossibleCryptoKeyPair = null
    private cachedJwk: PossibleJsonWebKey = null
    private cachedJkt: string | null = null

    // TODO: Clean up code.
    constructor(
        private readonly store: DpopKeyStoreService,
        private readonly logger: CryptoTraderLoggerService,
    ) {
        this.logger.setContext(LoggerContext.Dpop)
    }

    /** Ensure a keypair exists. */
    public async ensureKeys(): Promise<void> {
        if (this.keyPair) return

        this.logger.debug(`Ensuring keys...`)

        // Try to load from IndexedDB if enabled
        if (environment.persistDpopKey) {
            try {
                const loaded: PossibleCryptoKeyPair = await this.store.load()
                if (loaded) {
                    this.logger.info(`Keys loaded from store.`)
                    this.keyPair = loaded
                    this.cachedJwk = null
                    this.cachedJkt = null
                    return
                }
            } catch {
                this.logger.warn(`Failed to load keys from store.`)
            }
        }

        this.logger.info(`Generating new key pair...`)
        // Generate a new EC P-256 key pair for ES256
        this.keyPair = await crypto.subtle.generateKey(
            {
                name: 'ECDSA',
                namedCurve: 'P-256',
            },
            false, // private key non-extractable
            ['sign', 'verify'],
        )

        // Persist if enabled
        if (environment.persistDpopKey) {
            try {
                await this.store.save(this.keyPair)
            } catch {
                /* ignore */
            }
        }

        this.cachedJwk = null
        this.cachedJkt = null
    }

    /**
     * Export the public key as a JWK (cached).
     *
     * @returns The public key as a JWK.
     */
    public async getPublicJwk(): Promise<JsonWebKey> {
        await this.ensureKeys()
        if (!this.cachedJwk) {
            // TODO: Remove or alter conditions to prevent non-null assertion.
            this.cachedJwk = await crypto.subtle.exportKey('jwk', this.keyPair!.publicKey)
            // normalize fields for EC
            this.cachedJwk.kty = 'EC'
            this.cachedJwk.alg = 'ES256'
            this.cachedJwk.key_ops = ['verify']
            this.cachedJwk.ext = true
        }
        return this.cachedJwk
    }

    /**
     * Compute and cache the RFC7638 JWK thumbprint (jkt).
     *
     * @returns The RFC7638 JWK thumbprint (jkt).
     */
    public async getJkt(): Promise<string> {
        if (this.cachedJkt) return this.cachedJkt
        const jwk: JsonWebKey = await this.getPublicJwk()
        // Canonical JSON with required members for EC in lexicographic order: {"crv":"P-256","kty":"EC","x":"...","y":"..."}
        const canonical: string = `{"crv":"${jwk.crv}","kty":"EC","x":"${jwk.x}","y":"${jwk.y}"}`
        const digest: ArrayBuffer = await crypto.subtle.digest(
            'SHA-256',
            new TextEncoder().encode(canonical),
        )
        this.cachedJkt = base64url(new Uint8Array(digest))
        return this.cachedJkt
    }

    /**
     * Sign an input (string) using the private key (ECDSA P-256), returning base64url(JOSE r|s) signature
     * @param input
     * @returns base64url(JOSE r|s) signature
     */
    public async signInput(input: string): Promise<string> {
        await this.ensureKeys()
        const data: Uint8Array<ArrayBuffer> = new TextEncoder().encode(input)
        const derSig: ArrayBuffer = await crypto.subtle.sign(
            { name: 'ECDSA', hash: 'SHA-256' },
            // TODO: Remove or alter conditions to prevent non-null assertion.
            this.keyPair!.privateKey,
            data,
        )
        const jose: Uint8Array<ArrayBufferLike> = derToJose(new Uint8Array(derSig), 32)
        return base64url(jose)
    }

    /** Reset and forget keys (e.g., on logout). */
    public clear(): void {
        this.keyPair = null
        this.cachedJwk = null
        this.cachedJkt = null
        if (environment.persistDpopKey) {
            // Fire-and-forget
            void this.store.clear()
        }
    }
}

/**
 * Builds a base64 url-encoded string from binary or string input.
 *
 * @param bytesOrString
 * @returns base64 url-encoded string
 */
export function base64url(bytesOrString: Uint8Array | string): string {
    let bytes: Uint8Array
    if (typeof bytesOrString === 'string') {
        bytes = new TextEncoder().encode(bytesOrString)
    } else {
        bytes = bytesOrString
    }
    let str: string = ''
    const chunk: number = 0x8000
    for (let i: number = 0; i < bytes.length; i += chunk) {
        str += String.fromCharCode.apply(
            null,
            Array.from(bytes.subarray(i, i + chunk)),
        )
    }
    const b64: string = btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '')
    return b64
}

/**
 * Convert ASN.1/DER ECDSA signature to JOSE r|s raw format of fixed size. Accepts raw r||s too.
 *
 * @param derSignature
 * @param coordinateSize
 * @returns The JOSE r|s raw format of fixed size.
 */
function derToJose(derSignature: Uint8Array, coordinateSize: number = 32): Uint8Array {
    // Accept already-raw signatures (r||s) of expected length
    if (derSignature.length === coordinateSize * 2) {
        return derSignature
    }

    // Minimal ASN.1 DER parser for ECDSA signatures: SEQUENCE( r INTEGER, s INTEGER )
    let offset: number = 0
    const buffer: Uint8Array<ArrayBufferLike> = derSignature

    // Expect DER SEQUENCE (0x30)
    if (buffer[offset++] !== 0x30) {
        throw new Error('Invalid ECDSA signature format')
    }

    // Read DER length (short or long form)
    const lengthByte: number = buffer[offset++]
    let sequenceLength: number = 0
    if (lengthByte < 0x80) {
        sequenceLength = lengthByte
    } else {
        const numLenBytes: number = lengthByte & 0x7f
        sequenceLength = 0
        for (let i: number = 0; i < numLenBytes; i++) {
            sequenceLength = (sequenceLength << 8) | buffer[offset++]
        }
    }

    if (buffer[offset++] !== 0x02) throw new Error('Invalid DER r INTEGER')
    const rLength: number = buffer[offset++]
    let rInteger: Uint8Array<ArrayBufferLike> = buffer.slice(offset, offset + rLength)
    offset += rLength

    if (buffer[offset++] !== 0x02) throw new Error('Invalid DER s INTEGER')
    const sLength: number = buffer[offset++]
    let sInteger: Uint8Array<ArrayBufferLike> = buffer.slice(offset, offset + sLength)

    // Strip leading zeros if present (ensures positive INTEGER)
    if (rInteger.length > 0 && rInteger[0] === 0x00) {
        rInteger = rInteger.slice(1)
    }
    if (sInteger.length > 0 && sInteger[0] === 0x00) {
        sInteger = sInteger.slice(1)
    }

    // Left pad to fixed coordinate size
    const rPadded: Uint8Array<ArrayBuffer> = new Uint8Array(coordinateSize)
    if (rInteger.length > coordinateSize) {
        rInteger = rInteger.slice(rInteger.length - coordinateSize)
    }
    rPadded.set(rInteger, coordinateSize - rInteger.length)

    const sPadded: Uint8Array<ArrayBuffer> = new Uint8Array(coordinateSize)
    if (sInteger.length > coordinateSize) {
        sInteger = sInteger.slice(sInteger.length - coordinateSize)
    }
    sPadded.set(sInteger, coordinateSize - sInteger.length)

    const joseSignature: Uint8Array<ArrayBuffer> = new Uint8Array(coordinateSize * 2)
    joseSignature.set(rPadded, 0)
    joseSignature.set(sPadded, coordinateSize)
    return joseSignature
}
