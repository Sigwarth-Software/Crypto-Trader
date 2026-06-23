import { Injectable } from '@angular/core'

import { WebSocketService } from '@theoliverlear/angular-suite'
import { environment } from '@environments/environment'
import { resolveWebSocketUrl } from '@services/net/url-resolver'

@Injectable({
    providedIn: 'root',
})
export class CurrencyValueWsService extends WebSocketService<string, string> {
    private static readonly URL: string = resolveWebSocketUrl(
        `${environment.websocketUrl}/currency/value`,
    )

    constructor() {
        super(CurrencyValueWsService.URL)
    }
}
