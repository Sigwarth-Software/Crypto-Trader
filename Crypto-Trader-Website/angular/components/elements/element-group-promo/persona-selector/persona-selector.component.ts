import { Component } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'
import {
    personaBelieverIcon,
    personaStrategistIcon,
    personaEqualizerIcon,
    personaBuilderIcon,
    personaTycoonIcon,
} from '@assets/image.assets'
import { Persona } from './models/Persona'

/**
 * A component that allows the user to select a persona.
 */
@Component({
    selector: 'persona-selector',
    standalone: false,
    templateUrl: './persona-selector.component.html',
    styleUrls: ['./persona-selector.component.scss'],
})
export class PersonaSelectorComponent {
    protected activeIndex: number = 0
    // TODO: Move to assets file.
    protected personas: Persona[] = [
        {
            id: 'believer',
            name: 'The Believer',
            tagline: 'Not much to invest — but every stake matters.',
            description:
                'New to crypto with a small budget. You want to grow your holdings passively without staring at charts all day. You trust in the long game — and Crypto Trader runs it for you.',
            icon: personaBelieverIcon,
            accentColor: 'believer',
            suggestedTier: 'Free',
            traits: ['Passive income', 'Small budget', 'Long-term holder', 'Set and forget'],
        },
        {
            id: 'strategist',
            name: 'The Strategist',
            tagline: 'Data over gut feeling. Always.',
            description:
                "You don't gamble — you calculate. ML-powered price prediction, multi-timeframe analysis, and confidence-scored signals give you the edge that spreadsheets never could.",
            icon: personaStrategistIcon,
            accentColor: 'strategist',
            suggestedTier: 'Pro',
            traits: ['Data-driven', 'ML signals', 'Risk-managed', 'Multi-timeframe'],
        },
        {
            id: 'equalizer',
            name: 'The Equalizer',
            tagline: 'Crypto is for everyone — and so is great trading software.',
            description:
                "You believe financial tools shouldn't be locked behind six-figure minimums. Institutional-grade algorithms at zero profit margin — because the value belongs to you, not us.",
            icon: personaEqualizerIcon,
            accentColor: 'equalizer',
            suggestedTier: 'Pro',
            traits: ['Democratizer', 'Zero-margin', 'Community-first', 'Open access'],
        },
        {
            id: 'builder',
            name: 'The Builder',
            tagline: 'If I can read the code, I can trust the code.',
            description:
                "Open-source isn't a feature for you — it's a requirement. You want to inspect every algorithm, fork the repository, extend modules, and know exactly how your money is being managed.",
            icon: personaBuilderIcon,
            accentColor: 'builder',
            suggestedTier: 'Pro',
            traits: ['Open-source', 'Developer', 'Auditor', 'Extendable'],
        },
        {
            id: 'tycoon',
            name: 'The Tycoon',
            tagline: 'Only the best trades. Only the best tools.',
            description:
                'You trade at volume and demand every edge. Beast Mode execution on many threads, AI Chat for real-time analysis, news sentiment, and custom strategies — the full arsenal, no compromises.',
            icon: personaTycoonIcon,
            accentColor: 'tycoon',
            suggestedTier: 'Ultimate',
            traits: ['Beast Mode', 'AI Chat', 'High-volume', 'Full arsenal'],
        },
    ]

    /**
     * Gets the currently active persona.
     * @returns The currently active persona.
     */
    public get active(): Persona {
        return this.personas[this.activeIndex]
    }

    /**
     * Selects a persona by index.
     *
     * @param index
     */
    public select(index: number): void {
        this.activeIndex = index
    }

    protected readonly TagType: typeof TagType = TagType
}
