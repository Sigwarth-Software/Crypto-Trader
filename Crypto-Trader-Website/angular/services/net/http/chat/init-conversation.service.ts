import { Injectable } from "@angular/core"
import { HttpClientService } from '@theoliverlear/angular-suite'
import { InitConversationResponse } from '@models/chat/types'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

@Injectable({
    providedIn: 'root',
})
export class InitConversationService extends HttpClientService<never, InitConversationResponse> {
    private static readonly URL: string = `${environment.apiUrl}/chat/init`

    constructor() {
        super(InitConversationService.URL)
    }

    initConversation(): Observable<InitConversationResponse> {
        return this.get()
    }
}