// chat.component.ts
import { Component, OnInit } from '@angular/core';

import { chatModuleIcon, ImageAsset } from '@assets/image.assets';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';
import { ChatConversation, ChatMessage } from '@models/chat/types';
import {LoggerContext} from "@models/logging/LoggerContext";

@Component({
    selector: 'chat',
    templateUrl: './chat.component.html',
    styleUrls: ['./chat.component.scss'],
    standalone: false,
})
export class ChatComponent implements OnInit {
    protected readonly chatIcon: ImageAsset = chatModuleIcon;
    protected conversations: ChatConversation[] = [];
    protected activeConversation: ChatConversation | null = null;
    protected userInput: string = '';
    protected isLoading: boolean = false;

    constructor(private readonly logger: CryptoTraderLoggerService) {}

    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.Chat)
    }

    public startNewConversation(): void {
        this.logger.debug('Starting new conversation');
        const conversation: ChatConversation = {
            id: crypto.randomUUID(),
            title: 'New Chat',
            messages: [],
            updatedAt: new Date(),
        };
        this.conversations.unshift(conversation);
        this.activeConversation = conversation;
    }

    public selectConversation(conversation: ChatConversation): void {
        this.logger.debug(`Selecting conversation: ${conversation.id}`);
        this.activeConversation = conversation;
    }

    public deleteConversation(conversation: ChatConversation, event: MouseEvent): void {
        this.logger.debug(`Deleting conversation: ${conversation.id}`);
        event.stopPropagation();
        this.conversations = this.conversations.filter(
            (c: ChatConversation): boolean => c.id !== conversation.id,
        );
        if (this.activeConversation?.id === conversation.id) {
            this.activeConversation = this.conversations.length > 0 ? this.conversations[0] : null;
        }
    }

    public sendMessage(): void {
        const text: string = this.userInput.trim();
        if (!text || !this.activeConversation) {
            this.logger.warn('Attempted to send empty message or no active conversation');
            return;
        }
        this.logger.info('Sending user message');
        this.activeConversation.messages.push({ role: 'user', content: text, timestamp: new Date() });
        if (this.activeConversation.title === 'New Chat') {
            this.activeConversation.title = text.length > 30 ? text.substring(0, 30) + '...' : text;
        }
        this.activeConversation.updatedAt = new Date();
        this.userInput = '';
        this.isLoading = true;
        this.logger.debug('Waiting for assistant response...');
        // TODO: Connect to ChatService backend
        setTimeout((): void => {
            if (this.activeConversation) {
                this.logger.info('Received mock assistant response');
                this.activeConversation.messages.push({
                    role: 'assistant',
                    content: 'Chat backend is not yet connected.',
                    timestamp: new Date(),
                });
            }
            this.isLoading = false;
        }, 500);
    }

    public onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }
}
