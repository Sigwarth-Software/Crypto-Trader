import { Injectable } from '@angular/core'
import { ConversationResponse } from '@models/chat/types'
import { HttpClientService } from '@theoliverlear/angular-suite'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

@Injectable({
    providedIn: 'root',
})
export class ListConversationsService extends HttpClientService<never, ConversationResponse[]> {
    private static readonly URL: string = `${environment.apiUrl}/chat/list`

    constructor() {
        super(ListConversationsService.URL)
    }

    getConversationList(): Observable<ConversationResponse[]> {
        return this.get()
    }
}