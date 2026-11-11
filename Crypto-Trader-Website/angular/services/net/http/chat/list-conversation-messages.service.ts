import { Injectable } from '@angular/core'
import { HttpClientService } from '@theoliverlear/angular-suite'
import { ConversationMessageResponse } from '@models/chat/types'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

/**
 * Calls the API to list the messages in a conversation.
 */
@Injectable({
    providedIn: 'root',
})
export class ListConversationMessagesService extends HttpClientService<
    never,
    ConversationMessageResponse[]
> {
    private static readonly URL: string = `${environment.apiUrl}/chat`

    constructor() {
        super(ListConversationMessagesService.URL)
    }

    getConversationMessagesList(conversationId: string): Observable<ConversationMessageResponse[]> {
        this.url = `${ListConversationMessagesService.URL}/${conversationId}/messages`
        return this.get()
    }
}