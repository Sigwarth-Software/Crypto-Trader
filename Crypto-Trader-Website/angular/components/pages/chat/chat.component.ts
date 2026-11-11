// chat.component.ts
import { Component, OnInit } from '@angular/core'

import { chatModuleIcon, ImageAsset } from '@assets/image.assets'
import {
    ChatConversation,
    ChatMessage,
    ConversationMessageResponse,
    ConversationResponse,
    InitConversationResponse,
    MessageAuthorSource,
} from '@models/chat/types'
import { LoggerContext } from '@models/logging/LoggerContext'
import { BaseLoggingElementComponent } from '@components/elements/element-group-system/base-logging-element/base-logging-element.component'
import { InitConversationService } from '@http/chat/init-conversation.service'
import {
    ChatStreamingService
} from '@services/net/sse/chat/chat-streaming.service'
import { ElementSize } from '@theoliverlear/angular-suite'
import {
    LoadingWheelColorScheme
} from '@components/elements/element-group-system/loading-wheel/models/LoadingWheelColorScheme'
import {
    ListConversationsService
} from '@http/chat/list-conversations.service'
import {
    ListConversationMessagesService
} from '@http/chat/list-conversation-messages.service'

/**
 * Chat page for interacting with Crypto-Trader-Chat.
 */
@Component({
    selector: 'chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
    standalone: false,
})
export class ChatComponent extends BaseLoggingElementComponent implements OnInit {
    protected readonly chatIcon: ImageAsset = chatModuleIcon
    protected conversations: ChatConversation[] = []
    protected activeConversation: ChatConversation | null = null
    protected userInput: string = ''
    protected isLoading: boolean = false
    protected currentConversationId: string | null = null
    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly LoadingWheelColorScheme: typeof LoadingWheelColorScheme =
        LoadingWheelColorScheme

    constructor(
        private readonly initConversationService: InitConversationService,
        private readonly sseClientService: ChatStreamingService,
        private readonly listConversationsService: ListConversationsService,
        private readonly listConversationMessagesService: ListConversationMessagesService
    ) {
        super()
        this.loggerContext = LoggerContext.Chat
    }

    ngOnInit(): void {
        super.ngOnInit()
        this.listConversationsService
            .getConversationList()
            .subscribe((responses: ConversationResponse[]): void => {
                this.conversations = [
                    ...this.conversations,
                    ...responses.map((response): ChatConversation => {
                        return {
                            id: response.conversationId,
                            title: response.title,
                            messages: [],
                            updatedAt: new Date(),
                        }
                    })
                ].reverse()
                this.log.info(`Loaded ${this.conversations.length} conversations`)
            })
    }

    /**
     * Initializes a new conversation.
     */
    public startNewConversation(): void {
        this.initConversationService
            .initConversation()
            .subscribe((response: InitConversationResponse): void => {
                this.currentConversationId = response.conversationId
                this.log.info(`Initialized new conversation with ID: ${this.currentConversationId}`)
            })
        this.log.debug('Starting new conversation')
        const conversation: ChatConversation = {
            id: crypto.randomUUID(),
            title: 'New Chat',
            messages: [],
            updatedAt: new Date(),
        }
        this.conversations.unshift(conversation)
        this.activeConversation = conversation
    }

    /**
     * Selects a conversation from the list of conversations.
     * @param conversation
     */
    public selectConversation(conversation: ChatConversation): void {
        this.log.debug(`Selecting conversation: ${conversation.id}`)

        if (this.activeConversation !== conversation) {
            this.activeConversation = conversation
        }
        this.listConversationMessagesService.getConversationMessagesList(conversation.id).subscribe((messages: ConversationMessageResponse[]): void => {
            if (!this.activeConversation) {
                this.activeConversation = conversation
            }

            if (this.activeConversation.messages.length > 0) {
                this.activeConversation.messages = []
            }
            this.activeConversation.messages = messages.map((message: ConversationMessageResponse): ChatMessage => {
                return {
                    role: message.author === MessageAuthorSource.User ? 'user' : 'assistant',
                    content: message.messageContent,
                    timestamp: new Date(),
                }
            })
        })
    }

    /**
     * Deletes a conversation from the list of conversations.
     * @param conversation
     * @param event
     */
    public deleteConversation(conversation: ChatConversation, event: MouseEvent): void {
        this.log.debug(`Deleting conversation: ${conversation.id}`)
        event.stopPropagation()
        this.conversations = this.conversations.filter(
            (c: ChatConversation): boolean => c.id !== conversation.id,
        )

        if (this.activeConversation?.id === conversation.id) {
            this.activeConversation = this.conversations.length > 0 ? this.conversations[0] : null
        }
    }

    /**
     * Sends a message to the active conversation.
     */
    public sendMessage(): void {
        if (!this.currentConversationId) {
            this.log.error('No current conversation ID. Cannot send message.')
            return
        }

        const text: string = this.userInput.trim()

        if (!text || !this.activeConversation) {
            this.log.warn('Attempted to send empty message or no active conversation')
            return
        }
        this.log.info('Sending user message')
        this.activeConversation.messages.push({
            role: 'user',
            content: text,
            timestamp: new Date(),
        })

        if (this.activeConversation.title === 'New Chat') {
            this.activeConversation.title = text.length > 30 ? `${text.substring(0, 30)}...` : text
        }
        this.activeConversation.updatedAt = new Date()
        this.userInput = ''
        this.isLoading = true
        this.log.debug('Waiting for assistant response...')
        setTimeout((): void => {
            if (this.activeConversation) {
                if (this.currentConversationId) {
                    let streamedMessage: ChatMessage | null = null
                    this.sseClientService.sendMessage({ messageContent: text }, this.currentConversationId).subscribe({
                        next: (delta: string): void => {
                            if (!this.activeConversation) {
                                return
                            }
                            if (!streamedMessage) {
                                streamedMessage = {
                                    role: 'assistant',
                                    content: '',
                                    timestamp: new Date(),
                                }
                                this.activeConversation.messages.push(streamedMessage)
                            }
                            streamedMessage.content += delta
                        },
                    })}
            }
            this.isLoading = false
        }, 500)
    }

    /**
     * On key down event, if the key is Enter and Shift is not pressed,
     * send the message.
     *
     * @param event
     */
    public onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault()
            this.sendMessage()
        }
    }
}
