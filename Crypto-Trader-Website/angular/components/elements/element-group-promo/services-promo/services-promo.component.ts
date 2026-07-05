import { Component } from '@angular/core';
import { TagType } from '@theoliverlear/angular-suite';
import {
    apiModuleIcon,
    securityModuleIcon,
    contactModuleIcon,
    chatModuleIcon,
} from '@assets/image.assets';
import { ModuleInfo } from '@models/module/ModuleInfo';
@Component({
    selector: 'services-promo',
    standalone: false,
    templateUrl: './services-promo.component.html',
    styleUrls: ['./services-promo.component.scss'],
})
export class ServicesPromoComponent {
    constructor() {}


    protected readonly TagType = TagType;

    // TODO: Move to assets file.
    protected readonly serviceModules: ModuleInfo[] = [
        {
            name: 'Api',
            icon: apiModuleIcon,
            tagline: 'The Platform Hub',
            description:
                'Core backend orchestrating identity, portfolios, subscriptions, and cross-module communication.',
            features: [
                'User identity & session management',
                'Portfolio CRUD with real-time valuation',
                'Subscription tier enforcement',
            ],
            techStack: ['Java 23', 'Spring Boot'],
            accentColor: 'ocean-blue',
        },
        {
            name: 'Security',
            icon: securityModuleIcon,
            tagline: 'Defense in Depth',
            description:
                'Centralized auth, request filters, threat detection, and security event auditing across every endpoint.',
            features: [
                'JWT + DPoP token authentication',
                'Request filtering & rate limiting',
                'Threat detection & security events',
            ],
            techStack: ['Kotlin', 'Spring Security'],
            accentColor: 'teal-green',
        },
        {
            name: 'Contact',
            icon: contactModuleIcon,
            tagline: 'Outbound Messaging',
            description:
                'Event-driven email delivery — templated notifications, account alerts, and transactional messages via Amazon SES.',
            features: [
                'Thymeleaf-powered email templates',
                'Event-driven send triggers',
                'Amazon SES integration',
            ],
            techStack: ['Java', 'Thymeleaf', 'SES'],
            accentColor: 'sunbeam-yellow',
        },
        {
            name: 'Chat',
            icon: chatModuleIcon,
            tagline: 'AI-Powered Assistant',
            description:
                'Conversational chatbot via MCP Client — natural language queries about your portfolio, trades, and market data.',
            features: [
                'LLM-powered natural language interface',
                'MCP Server integration',
                'Context-aware portfolio queries',
            ],
            techStack: ['LLM', 'OpenAI', 'MCP'],
            accentColor: 'mint-green',
        },
    ];
}
