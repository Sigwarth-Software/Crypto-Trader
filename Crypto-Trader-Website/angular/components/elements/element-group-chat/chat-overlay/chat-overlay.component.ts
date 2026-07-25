// chat-overlay.component.ts
import { Component, OnInit } from '@angular/core'

import { chatModuleIcon, ImageAsset } from '@assets/image.assets'
import { LoggedInService } from '@http/auth/status/logged-in.service'
import { SubscriptionTierService } from '@http/user/subscription-tier.service'
import { SubscriptionTierResponse } from '@models/user/types'
import { ChatMessage } from '@models/chat/types'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'
import { ElementSize } from '@theoliverlear/angular-suite'
import {
    LoadingWheelColorScheme
} from '@components/elements/element-group-system/loading-wheel/models/LoadingWheelColorScheme'

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
    protected readonly chatIcon: ImageAsset = chatModuleIcon
    protected isOpen: boolean = false
    protected messages: ChatMessage[] = []
    protected userInput: string = ''
    protected isLoading: boolean = false
    protected isVisible: boolean = false
    private isLoggedIn: boolean = false
    private isUltimate: boolean = false

    constructor(
        private readonly loggedInService: LoggedInService,
        private readonly subscriptionTierService: SubscriptionTierService,
        private readonly log: CryptoTraderLoggerService,
    ) {}

    /**
     * Initializes listening for authentication and subscription tier changes
     * to determine visibility of chat overlay.
     */
    public ngOnInit(): void {
        this.log.setContext(LoggerContext.Chat)
        this.listenForAuthStatus()
        this.listenForSubscriptionTier()
    }

    private listenForAuthStatus(): void {
        this.log.log('Listening for auth status for chat overlay display.')
        this.loggedInService.getAuthState().subscribe((authStatus: boolean): void => {
            this.log.log(`Observed auth status: ${authStatus}`)
            this.isLoggedIn = authStatus
            this.updateVisibility()
        })
    }

    private listenForSubscriptionTier(): void {
        this.log.log('Listening for subscription tier for chat overlay display.')
        this.subscriptionTierService
            .getSubscriptionTier()
            .subscribe((tier: SubscriptionTierResponse): void => {
                this.isUltimate = tier.subscriptionTier === 'ULTIMATE'
                this.updateVisibility()
            })
    }

    private updateVisibility(): void {
        this.isVisible = this.isLoggedIn && this.isUltimate
    }

    /**
     * Toggles the visibility of the chat overlay.
     */
    public toggle(): void {
        this.isOpen = !this.isOpen
        this.log.debug(`Chat overlay toggled. Open: ${this.isOpen}`)
    }

    /**
     * Sends a message to the chat back-end.
     */
    public sendMessage(): void {
        const text: string = this.userInput.trim()
        if (!text) {
            return
        }
        this.log.info('Sending message from chat overlay')
        this.messages.push({ role: 'user', content: text, timestamp: new Date() })
        this.userInput = ''
        this.isLoading = true
        // TODO: Connect to ChatService backend
        setTimeout((): void => {
            this.log.debug('Received mock response in chat overlay')
            this.messages.push({
                role: 'assistant',
                content: 'Chat backend is not yet connected.',
                timestamp: new Date(),
            })
            this.isLoading = false
        }, 500)
    }

    /**
     * If the user presses enter, send the message.
     * @param event
     */
    public onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault()
            this.sendMessage()
        }
    }

    protected readonly ElementSize: typeof ElementSize = ElementSize
    protected readonly LoadingWheelColorScheme: typeof LoadingWheelColorScheme =
        LoadingWheelColorScheme
}
