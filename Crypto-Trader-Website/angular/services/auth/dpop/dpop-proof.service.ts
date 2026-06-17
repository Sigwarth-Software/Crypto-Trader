import { Injectable } from '@angular/core'

import { base64url, DpopKeyService } from './dpop-key.service'

/**
 *
 */
@Injectable({
    providedIn: 'root',
})
export class DpopProofService {
    // TODO: Clean up code.
    constructor(private readonly keys: DpopKeyService) {}

    /**
     * Build a DPoP proof JWT (compact JWS) for the given request
     * method and absolute URL. Optionally include 'ath' for the
     * presented access token.
     * @param method
     * @param absoluteUrl
     * @param accessToken
     * @returns The DPoP proof.
     */
    public async buildProof(
        method: string,
        absoluteUrl: string,
        accessToken?: string,
    ): Promise<string> {
        await this.keys.ensureKeys()
        const jwk: JsonWebKey = await this.keys.getPublicJwk()

        const header: {
            readonly alg: 'ES256'
            readonly jwk: JsonWebKey
            readonly typ: 'dpop+jwt'
        } = {
            alg: 'ES256',
            typ: 'dpop+jwt',
            jwk,
        } as const

        const now: number = Math.floor(Date.now() / 1000)
        const payload: Record<string, any> = {
            htm: method,
            htu: absoluteUrl,
            iat: now,
            jti: this.generateNonce(),
        }
        if (accessToken) {
            payload.ath = await this.sha256Base64url(accessToken)
        }

        const encodedHeader: string = base64url(JSON.stringify(header))
        const encodedPayload: string = base64url(JSON.stringify(payload))
        const signingInput: string = `${encodedHeader}.${encodedPayload}`
        const signature: string = await this.keys.signInput(signingInput)
        return `${encodedHeader}.${encodedPayload}.${signature}`
    }

    /**
     * Generate a UUID v4 nonce for DPoP jti.
     * @returns A nonce.
     */
    private generateNonce(): string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
            /[xy]/g,
            (patternChar: string): string => {
                const randomNibble: number = crypto.getRandomValues(new Uint8Array(1))[0] & 0x0f
                const selectedNibble: number =
                    patternChar === 'x' ? randomNibble : (randomNibble & 0x3) | 0x8
                return selectedNibble.toString(16)
            },
        )
    }

    /**
     * Compute base64url(SHA-256(input)) used for the optional 'ath' claim.
     * @param input
     * @returns The SHA of a base64 URL.
     */
    private async sha256Base64url(input: string): Promise<string> {
        const bytes: Uint8Array<ArrayBuffer> = new TextEncoder().encode(input)
        const digest: ArrayBuffer = await crypto.subtle.digest('SHA-256', bytes)
        return base64url(new Uint8Array(digest))
    }
}
