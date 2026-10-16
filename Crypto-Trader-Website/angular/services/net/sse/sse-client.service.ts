import { inject, Injectable, NgZone } from '@angular/core'
import { PossibleToken } from '@models/auth/types'
import { TokenStorageService } from '@auth/token-storage.service'
import { firstValueFrom, Observable } from "rxjs"
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { DpopProofService } from '@auth/dpop/dpop-proof.service'
import { resolveAbsoluteHttpUrl } from '@app/scripts/url-resolver.script'
import { TokenRefreshService } from '@http/auth/token/token-refresh.service'

@Injectable({
    providedIn: 'root',
})
export class SseClientService<T> {
    private bearerToken: PossibleToken = null

    constructor(
        private url: string,
        private readonly tokenStorageService: TokenStorageService = inject<TokenStorageService>(
            TokenStorageService,
        ),
        private readonly zone: NgZone = inject<NgZone>(NgZone),
        private readonly dpopProofService: DpopProofService = inject<DpopProofService>(DpopProofService),
        private readonly tokenRefreshService: TokenRefreshService = inject<TokenRefreshService>(TokenRefreshService),
    ) {}

    setUrl(url: string): void {
        this.url = url
    }

    protected post(payload: T): Observable<string> {
        return new Observable<string>((observer): (() => void) => {
            const controller = new AbortController()
            void this.ensureFreshToken()
                .then((): Promise<string> =>
                    this.dpopProofService.buildProof('POST', resolveAbsoluteHttpUrl(this.url), this.bearerToken ?? undefined),
                )
                .then((proof: string): Promise<void> =>
                    fetchEventSource(this.url, {
                        method: 'POST',
                        body: JSON.stringify(payload),
                        headers: {
                            'Content-Type': 'application/json',
                            Authorization: `DPoP ${this.bearerToken}`,
                            DPoP: proof,
                        },
                        signal: controller.signal,
                        onmessage: (message): void => {
                            if (message.data === '[DONE]') {
                                this.zone.run((): void => observer.complete())
                            }

                            try {
                                const parsed: any = JSON.parse(message.data)
                                const delta: any = parsed.choices?.[0].delta.content

                                if (delta) {
                                    this.zone.run((): void => observer.next(delta as string))
                                }
                            } catch {
                                this.zone.run((): void => observer.next(message.data))
                            }
                        },
                        onerror: (err): never => {
                            this.zone.run((): void => observer.error(err))
                            throw err // Halts retry loop on failure
                        },
                        onclose: (): void => {
                            this.zone.run((): void => observer.complete())
                        },
                    }),
                )
                .catch((error: unknown): void => {
                    this.zone.run((): void => observer.error(error))
                })

            return (): void => controller.abort()
        })
    }
    private get(): Observable<string> {
        throw new Error('Method not implemented.')
    }
    private put(payload: T): Observable<string> {
        throw new Error('Method not implemented.')
    }
    private delete(): Observable<string> {
        throw new Error('Method not implemented.')
    }

    private async ensureFreshToken(): Promise<void> {
        const storedToken: PossibleToken = this.tokenStorageService.getToken()

        if (storedToken && !this.isExpired(storedToken)) {
            this.bearerToken = storedToken
            return
        }
        this.bearerToken = await firstValueFrom(this.tokenRefreshService.refreshToken())
    }

    private isExpired(token: string): boolean {
        try {
            const payload: any = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
            const skewMilliseconds: number = 5000
            return Date.now() + skewMilliseconds >= (payload?.exp ?? 0) * 1000
        } catch {
            return true
        }
    }
}