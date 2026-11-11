import { Injectable } from '@angular/core'
import { SseClientService } from '@services/net/sse/sse-client.service'
import { ChatMessageRequest } from '@models/chat/types'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

@Injectable({
    providedIn: 'root'
})
export class ChatStreamingService extends SseClientService<ChatMessageRequest> {
    private static URL: string = `${environment.apiUrl}/chat`

    constructor() {
        super(ChatStreamingService.URL)
    }

    public sendMessage(request: ChatMessageRequest, conversationId: string): Observable<string> {
        this.setUrl(`${environment.apiUrl}/chat/${conversationId}/stream`)
        return this.post(request)
    }
}