import { Component } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'
import {
    websiteModuleIcon,
    adminModuleIcon,
    consoleModuleIcon,
    mobileModuleIcon,
} from '@assets/image.assets'
import { ModuleInfo } from '@models/module/ModuleInfo'

/**
 * A component for displaying an interface promo.
 */
@Component({
    selector: 'interface-promo',
    standalone: false,
    templateUrl: './interface-promo.component.html',
    styleUrls: ['./interface-promo.component.scss'],
})
export class InterfacePromoComponent {
    constructor() {}

    // TODO: Move to assets file.
    protected readonly interfaceModules: ModuleInfo[] = [
        {
            name: 'Website',
            icon: websiteModuleIcon,
            tagline: 'The Public Face',
            description:
                'Marketing site, documentation gateway, and the front door to every feature. Built with Angular 20 and SCSS.',
            features: [
                'Responsive marketing pages',
                'Real-time dashboard & portfolio views',
                'Interactive trading console',
                'GitHub Pages deployment',
            ],
            techStack: ['Angular 20', 'SCSS', 'TypeScript'],
        },
        {
            name: 'Admin',
            icon: adminModuleIcon,
            tagline: 'The Control Room',
            description:
                'Desktop portal for operators — dashboards, kill-switches, user management, and system health at a glance.',
            features: [
                'System-wide kill switches',
                'User & subscription management',
                'Real-time health monitoring',
                'Trading activity oversight',
            ],
            techStack: ['JavaFX', 'Spring Boot'],
        },
        {
            name: 'Console',
            icon: consoleModuleIcon,
            tagline: 'Power User Tools',
            description:
                'Command-line interface for advanced users — interactive REPL, fast actions, scripting, and platform introspection.',
            features: [
                'Interactive REPL environment',
                'Scriptable trading commands',
                'Platform introspection queries',
                'Safe controls with confirmation',
            ],
            techStack: ['CLI', 'REPL'],
        },
        {
            name: 'Mobile',
            icon: mobileModuleIcon,
            tagline: 'Trading On the Go',
            description:
                'Mobile companion app — monitoring, push notifications, quick controls, and portfolio snapshots from anywhere.',
            features: [
                'Portfolio monitoring & alerts',
                'Push notification integration',
                'Quick trade controls',
                'Biometric authentication',
            ],
            techStack: ['Mobile', 'Cross-Platform'],
        },
    ]

    protected readonly TagType: typeof TagType = TagType
}
