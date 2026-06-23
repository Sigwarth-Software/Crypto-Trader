import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
    HttpClientService,
    HttpOptions,
    httpOptionsWithCredentials,
} from '@theoliverlear/angular-suite';
import { environment } from '@environments/environment';
import { DpopKeyService } from '@auth/dpop/dpop-key.service';
import { DpopProofService } from '@auth/dpop/dpop-proof.service';
import { RefreshCoordinatorService } from '@auth/refresh/refresh-coordinator.service';
import { TokenStorageService } from '@auth/token-storage.service';
import { AuthResponse } from '@models/auth/types';
import { resolveAbsoluteHttpUrl } from '@services/net/url-resolver';

@Injectable({
    providedIn: 'root',
})
export class TokenRefreshService extends HttpClientService<any, AuthResponse> {
    private static readonly URL: string = `${environment.apiUrl}/auth/refresh`;
    constructor(
        private tokenStore: TokenStorageService,
        private keys: DpopKeyService,
        private proofs: DpopProofService,
        private coordinator: RefreshCoordinatorService,
    ) {
        super(TokenRefreshService.URL);
    }

    public refreshToken(): Observable<string> {
        // Wrap the actual refresh call with a cross-tab single-flight coordinator
        // to prevent concurrent refresh attempts that could reuse the same cookie
        // and trigger family revocation on the server.
        return this.coordinator.singleFlight(
            () =>
                new Observable<string>((subscriber) => {
                    (async () => {
                        await this.keys.ensureKeys();
                        const current = this.tokenStore.getToken() || undefined;
                        const dpop: string = await this.proofs.buildProof(
                            'POST',
                            resolveAbsoluteHttpUrl(TokenRefreshService.URL),
                            current,
                        );
                        const headers = new HttpHeaders({
                            DPoP: dpop,
                        });
                        const options: HttpOptions = {
                            ...httpOptionsWithCredentials,
                            headers,
                        };
                        this.post({}, true, options).subscribe({
                            next: (response) => {
                                if (response?.authorized && response?.token) {
                                    this.tokenStore.setToken(response.token);
                                    subscriber.next(response.token);
                                    subscriber.complete();
                                } else {
                                    this.tokenStore.clear();
                                    subscriber.error(new Error('Unauthorized'));
                                }
                            },
                            error: (error) => {
                                this.tokenStore.clear();
                                subscriber.error(error);
                            },
                        });
                    })();
                }),
        );
    }
}
