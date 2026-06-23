import { Component } from '@angular/core';
import { TagType } from '@theoliverlear/angular-suite';
import {
    libraryModuleIcon,
    healthModuleIcon,
    loggingModuleIcon,
    testingModuleIcon,
    coverageModuleIcon,
    versionModuleIcon,
    assetsModuleIcon,
    docsModuleIcon,
} from '@assets/image.assets';
import { ModuleInfo } from '@models/module/ModuleInfo';
@Component({
    selector: 'infra-promo',
    standalone: false,
    templateUrl: './infra-promo.component.html',
    styleUrls: ['./infra-promo.component.scss'],
})
export class InfraPromoComponent {
    constructor() {}


    protected readonly TagType = TagType;

    // TODO: Move to assets file.
    protected readonly infraModules: ModuleInfo[] = [
        {
            name: 'Library',
            icon: libraryModuleIcon,
            tagline: 'Shared building blocks and contracts across every module.',
            description: '',
            features: [],
            techStack: ['Java', 'Maven'],
        },
        {
            name: 'Health',
            icon: healthModuleIcon,
            tagline: 'Lightweight health checks and dependency probes.',
            description: '',
            features: [],
            techStack: ['Kotlin', 'HttpClient'],
        },
        {
            name: 'Logging',
            icon: loggingModuleIcon,
            tagline: 'Structured observability toolkit for consistent logs.',
            description: '',
            features: [],
            techStack: ['Java', 'Logback'],
        },
        {
            name: 'Testing',
            icon: testingModuleIcon,
            tagline: 'Shared test toolkit — assertions, fixtures, containers.',
            description: '',
            features: [],
            techStack: ['JUnit', 'Testcontainers'],
        },
        {
            name: 'Coverage',
            icon: coverageModuleIcon,
            tagline: 'Aggregated JaCoCo coverage across 30+ subprojects.',
            description: '',
            features: [],
            techStack: ['Maven', 'JaCoCo'],
        },
        {
            name: 'Version',
            icon: versionModuleIcon,
            tagline: 'Version metadata, release notes, and CI automation.',
            description: '',
            features: [],
            techStack: ['Picocli', 'SemVer'],
        },
        {
            name: 'Assets',
            icon: assetsModuleIcon,
            tagline: 'Shared UI assets — logos, icons, and loaders.',
            description: '',
            features: [],
            techStack: ['Java', 'JavaFX'],
        },
        {
            name: 'Docs',
            icon: docsModuleIcon,
            tagline: 'Documentation site — guides, API refs, module references.',
            description: '',
            features: [],
            techStack: ['MkDocs', 'GitHub Pages'],
        },
    ];
}
