// meet-crypto-trader-promo.component.ts
import { Component, Input } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'

/**
 * A component promoting an introduction to Crypto Trader.
 */
@Component({
    selector: 'meet-crypto-trader-promo',
    standalone: false,
    templateUrl: './meet-crypto-trader-promo.component.html',
    styleUrls: ['./meet-crypto-trader-promo.component.scss'],
})
export class MeetCryptoTraderPromoComponent {
    // TODO: Move to assets.
    @Input() public words: string[] = [
        'a security gateway',
        'a machine learning engine',
        'a real-time data backbone',
        'a trade execution core',
        'an AI-powered chatbot',
        'a health monitoring system',
        'an observability platform',
        'a desktop admin portal',
    ]
    @Input() public periodMs: number = 2200
    // TODO: Move to assets.
    @Input() public modules: string[] = [
        'Security',
        'Analysis & ML',
        'Data Pipeline',
        'Trade Engine',
        'API & Orchestration',
        'Admin Portal',
        'AI Chat',
        'Console & CLI',
        'Health Monitoring',
        'Logging & Observability',
        'Contact & Messaging',
        'Docs & Guides',
        'Coverage & Testing',
        'Version Intelligence',
        'Website & Marketing',
        'Assets & Design',
    ]

    @Input() public ctaRouterLink: string | null = '/modules'
    @Input() public ctaText: string = 'Explore the modules'

    protected readonly TagType: typeof TagType = TagType
}
