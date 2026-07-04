import { Injectable } from '@angular/core';

import { WebSocketService } from '@theoliverlear/angular-suite';
import { environment } from '@environments/environment';
import { AuthResponse, SignupRequest } from '@models/auth/types';
import { resolveWebSocketUrl } from '@app/scripts/url-resolver.script';

/**
 * WebSocket client for signup events/messages.
 *
 * The application now performs signup over HTTP with DPoP to obtain sender-constrained
 * access tokens and set the HttpOnly refresh cookie. This WS client is kept for
 * auxiliary real-time flows and should not be used to transmit credentials in production.
 */
@Injectable({
    providedIn: 'root',
})
export class SignupWsService extends WebSocketService<
    SignupRequest,
    AuthResponse
> {
    private static readonly URL: string = resolveWebSocketUrl(
        `${environment.websocketUrl}/signup`,
    );
    constructor() {
        super(SignupWsService.URL);
    }
}
