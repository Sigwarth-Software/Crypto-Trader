import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
    HttpClientService,
    HttpOptions,
    httpOptionsWithCredentials,
} from '@theoliverlear/angular-suite';
import { environment } from '@environments/environment';
import { AuthResponse, LoginRequest, PersistMethod } from '@models/auth/types';
import { resolveAbsoluteHttpUrl } from '@services/net/url-resolver';

import { DpopKeyService } from '../../../../auth/dpop/dpop-key.service';
import { DpopProofService } from '../../../../auth/dpop/dpop-proof.service';
import { TokenStorageService } from '../../../../auth/token-storage.service';

@Injectable({
    providedIn: 'root',
})
export class LoginService extends HttpClientService<
    LoginRequest,
    AuthResponse
> {
    private static readonly URL: string = `${environment.apiUrl}/auth/login`;

    constructor(
        private tokenStore: TokenStorageService,
        private keys: DpopKeyService,
        private proofs: DpopProofService,
    ) {
        super(LoginService.URL);
    }

    public login(request: LoginRequest) {
        return new Observable<AuthResponse>((subscriber) => {
            (async () => {
                await this.keys.ensureKeys();
                const dpop: string = await this.proofs.buildProof(
                    'POST',
                    resolveAbsoluteHttpUrl(LoginService.URL),
                );
                const headers: HttpHeaders = new HttpHeaders({
                    DPoP: dpop,
                });
                const options: HttpOptions = {
                    ...httpOptionsWithCredentials,
                    headers,
                };
                this.post(request, true, options)
                    .pipe(
                        tap((response) => {
                            if (response?.authorized && response?.token) {
                                this.tokenStore.setToken(response.token as any);
                            }
                        }),
                    )
                    .subscribe({
                        next: (response) => {
                            subscriber.next(response);
                            subscriber.complete();
                        },
                        error: (error) => {
                            subscriber.error(error);
                        },
                    });
            })();
        });
    }
}
