// chat-overlay.component.ts
import { Component, OnInit } from '@angular/core';

import { chatModuleIcon, ImageAsset } from '@assets/image.assets';
import { LoggedInService } from '@http/auth/status/logged-in.service';
import { MySubscriptionTierService } from '@http/user/my-subscription-tier.service';
import { SubscriptionTier } from '@models/user/types';
import { ChatMessage } from '@models/chat/types';
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service';

/**
 * Chat popup for quick chat functionality.
 */
@Component({
    selector: 'chat-overlay',
    templateUrl: './chat-overlay.component.html',
    styleUrls: ['./chat-overlay.component.scss'],
    standalone: false,
})
export class ChatOverlayComponent implements OnInit {
    protected readonly chatIcon: ImageAsset = chatModuleIcon;
    protected isOpen: boolean = false;
    protected messages: ChatMessage[] = [];
    protected userInput: string = '';
    protected isLoading: boolean = false;
    protected isVisible: boolean = false;
    private isLoggedIn: boolean = false;
    private isUltimate: boolean = false;

    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly subscriptionTierService: MySubscriptionTierService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    /**
     * Initializes listening for authentication and subscription tier changes
     * to determine visibility of chat overlay.
     */
    public ngOnInit(): void {
        this.listenForAuthStatus();
        this.listenForSubscriptionTier();
    }

    private listenForAuthStatus(): void {
        this.log.log('Listening for auth status for chat overlay display.', 'ChatOverlay');
        this.loggedInService.getAuthState().subscribe((authStatus: boolean): void => {
            this.isLoggedIn = authStatus;
            this.updateVisibility();
        });
    }

    private listenForSubscriptionTier(): void {
        this.log.log('Listening for subscription tier for chat overlay display.', 'ChatOverlay');
        this.subscriptionTierService
            .getSubscriptionTierStream()
            .subscribe((tier: SubscriptionTier): void => {
                this.isUltimate = tier === 'ULTIMATE';
                this.updateVisibility();
            });
    }

    private updateVisibility(): void {
        this.isVisible = this.isLoggedIn && this.isUltimate;
    }

    /**
     * Toggles the visibility of the chat overlay.
     */
    public toggle(): void {
        this.isOpen = !this.isOpen;
        this.log.debug(`Chat overlay toggled. Open: ${this.isOpen}`, 'ChatOverlay');
    }

    /**
     * Sends a message to the chat back-end.
     */
    public sendMessage(): void {
        const text: string = this.userInput.trim();
        if (!text) {
            return;
        }
        this.log.info('Sending message from chat overlay', 'ChatOverlay');
        this.messages.push({ role: 'user', content: text, timestamp: new Date() });
        this.userInput = '';
        this.isLoading = true;
        // TODO: Connect to ChatService backend
        setTimeout((): void => {
            this.log.debug('Received mock response in chat overlay', 'ChatOverlay');
            this.messages.push({
                role: 'assistant',
                content: 'Chat backend is not yet connected.',
                timestamp: new Date(),
            });
            this.isLoading = false;
        }, 500);
    }

    /**
     * If the user presses enter, send the message.
     * @param event
     */
    public onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }
}
