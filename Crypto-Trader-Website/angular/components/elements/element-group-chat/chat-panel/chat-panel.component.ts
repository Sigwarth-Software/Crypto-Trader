// chat-panel.component.ts
import { Component } from '@angular/core'

import { chatModuleIcon, ImageAsset } from '@assets/image.assets'
import { ChatMessage } from '@models/chat/types'

/**
 * A component that has a panel for chats within Crypto-Trader-Chat.
 */
@Component({
    selector: 'chat-panel',
    templateUrl: './chat-panel.component.html',
    styleUrls: ['./chat-panel.component.scss'],
    standalone: false,
})
export class ChatPanelComponent {
    protected readonly chatIcon: ImageAsset = chatModuleIcon
    protected messages: ChatMessage[] = []
    protected userInput: string = ''
    protected isLoading: boolean = false

    constructor() {}

    /**
     * Sends a chat message to Crypto-Trader-Chat.
     */
    public sendMessage(): void {
        const text: string = this.userInput.trim()
        if (!text) {
            return
        }
        this.messages.push({ role: 'user', content: text, timestamp: new Date() })
        this.userInput = ''
        this.isLoading = true
        // TODO: Connect to ChatService backend
        setTimeout((): void => {
            this.messages.push({
                role: 'assistant',
                content: 'Chat backend is not yet connected.',
                timestamp: new Date(),
            })
            this.isLoading = false
        }, 500)
    }

    /**
     * On key down, listen for "enter" key input, and send the message if pressed.
     * @param event
     */
    public onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault()
            this.sendMessage()
        }
    }
}
