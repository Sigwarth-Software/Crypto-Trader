import { Component } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'
import { ArchitectureLayer } from '@components/elements/element-group-modules/module-architecture-map/module-architecture-map.component'

/**
 * A component for displaying an architecture promo.
 */
@Component({
    selector: 'architecture-promo',
    standalone: false,
    templateUrl: './architecture-promo.component.html',
    styleUrls: ['./architecture-promo.component.scss'],
})
export class ArchitecturePromoComponent {
    constructor() {}

    // TODO: Move to assets.
    protected readonly allModuleNames: string[] = [
        'Engine',
        'Analysis',
        'Data',
        'Api',
        'Security',
        'Contact',
        'Chat',
        'Website',
        'Admin',
        'Console',
        'Mobile',
        'Library',
        'Health',
        'Logging',
        'Testing',
        'Coverage',
        'Version',
        'Assets',
        'Docs',
    ]

    // TODO: Move to assets.
    protected readonly architectureLayers: ArchitectureLayer[] = [
        {
            label: 'Interfaces',
            nodes: [
                { name: 'Website' },
                { name: 'Admin' },
                { name: 'Console' },
                { name: 'Mobile' },
            ],
        },
        {
            label: 'Services',
            nodes: [
                { name: 'Api', highlight: true },
                { name: 'Security' },
                { name: 'Contact' },
                { name: 'Chat' },
            ],
        },
        {
            label: 'Trading Pipeline',
            nodes: [
                { name: 'Engine', highlight: true },
                { name: 'Analysis', highlight: true },
                { name: 'Data', highlight: true },
            ],
        },
        {
            label: 'Infrastructure',
            nodes: [
                { name: 'Library', small: true },
                { name: 'Health', small: true },
                { name: 'Logging', small: true },
                { name: 'Testing', small: true },
                { name: 'Coverage', small: true },
                { name: 'Version', small: true },
                { name: 'Assets', small: true },
                { name: 'Docs', small: true },
            ],
        },
    ]

    protected readonly TagType: typeof TagType = TagType
}
