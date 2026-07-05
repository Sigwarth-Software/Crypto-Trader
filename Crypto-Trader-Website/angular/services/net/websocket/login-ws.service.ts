import { Injectable } from '@angular/core';

import { WebSocketService } from '@theoliverlear/angular-suite';
import { environment } from '@environments/environment';
import { AuthResponse, LoginRequest } from '@models/auth/types';
import { resolveWebSocketUrl } from '@app/scripts/url-resolver.script';

/**
 * WebSocket client for the legacy login channel.
 *
 * Notes:
 * - The application now uses HTTP + DPoP for login. This WS client remains for backward
 *   compatibility and should not be used for authentication in production.
 * - If used, establish the socket only after a successful HTTP auth and do not send
 *   raw credentials over the wire.
 */
@Injectable({
    providedIn: 'root',
})
export class LoginWsService extends WebSocketService<
    LoginRequest,
    AuthResponse
> {
    private static readonly URL: string = resolveWebSocketUrl(
        `${environment.websocketUrl}/login`,
    );
    constructor() {
        super(LoginWsService.URL);
    }
}
