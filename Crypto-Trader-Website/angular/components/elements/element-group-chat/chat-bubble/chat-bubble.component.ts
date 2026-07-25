// chat-bubble.component.ts
import { Component, Input } from '@angular/core'

import { ChatMessage } from '@models/chat/types'

/**
 * A component showing Crypto-Trader-Chat bubbles.
 */
@Component({
    selector: 'chat-bubble',
    templateUrl: './chat-bubble.component.html',
    styleUrls: ['./chat-bubble.component.scss'],
    standalone: false,
})
export class ChatBubbleComponent {
    // TODO: Remove the non-null assertion.
    @Input() public message!: ChatMessage
}
