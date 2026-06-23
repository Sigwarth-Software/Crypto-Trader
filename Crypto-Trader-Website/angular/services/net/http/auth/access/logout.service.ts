import { HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpClientService, HttpOptions, httpOptionsWithCredentials } from '@theoliverlear/angular-suite';
import { environment } from '@environments/environment';
import { DpopKeyService } from '@auth/dpop/dpop-key.service';
import { DpopProofService } from '@auth/dpop/dpop-proof.service';
import { TokenStorageService } from '@auth/token-storage.service';
import { AuthResponse, PossibleToken } from '@models/auth/types';
import { resolveAbsoluteHttpUrl } from '@services/net/url-resolver';

import { LoggedInService } from '../status/logged-in.service';


@Injectable({
    providedIn: 'root',
})
export class LogoutService extends HttpClientService<any, AuthResponse> {
    private static readonly URL: string = `${environment.apiUrl}/auth/logout`;

    constructor(
        private tokenStore: TokenStorageService,
        private keys: DpopKeyService,
        private proofs: DpopProofService,
        private loggedInService: LoggedInService,
    ) {
        super(LogoutService.URL);
    }

    public logout(): Observable<AuthResponse> {
        return new Observable<AuthResponse>((subscriber): void => {
            void (async (): Promise<void> => {
                await this.keys.ensureKeys();
                const current: PossibleToken = this.tokenStore.getToken();
                const dpop: string = await this.proofs.buildProof(
                    'POST',
                    resolveAbsoluteHttpUrl(LogoutService.URL),
                );
                let headers: HttpHeaders = new HttpHeaders({
                    DPoP: dpop,
                });
                if (current) {
                    headers = headers.set('Authorization', `DPoP ${current}`);
                }
                this.tokenStore.clear();
                const options: HttpOptions = {
                    ...httpOptionsWithCredentials,
                    headers,
                };
                this.post({}, true, options).subscribe({
                    next: (response): void => {
                        this.keys.clear();
                        subscriber.next(response);
                        subscriber.complete();
                        this.loggedInService.isLoggedIn();
                    },
                    error: (error): void => {
                        this.keys.clear();
                        if (error.status !== 401) {
                            subscriber.error(error);
                        }
                    },
                });
            })();
        });
    }
}
