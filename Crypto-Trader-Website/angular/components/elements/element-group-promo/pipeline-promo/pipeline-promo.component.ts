import { Component } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'
import { analysisCodeSnippet, analysisCodeWindow } from '@assets/code-window.assets'
import { engineModuleIcon, analysisModuleIcon, dataModuleIcon } from '@assets/image.assets'
import { ModuleInfo } from '@models/module/ModuleInfo'
import { CodeWindow } from '@models/promo/types'

/**
 * A component for displaying a pipeline promo.
 */
@Component({
    selector: 'pipeline-promo',
    standalone: false,
    templateUrl: './pipeline-promo.component.html',
    styleUrls: ['./pipeline-promo.component.scss'],
})
export class PipelinePromoComponent {
    constructor() {}

    // TODO: Move to assets.
    protected readonly pipelineModules: ModuleInfo[] = [
        {
            name: 'Engine',
            icon: engineModuleIcon,
            tagline: 'The Execution Core',
            description:
                'Turns signals into live trades — position sizing, exchange orchestration, and strategy loops running around the clock.',
            features: [
                'Continuous trading loops with configurable intervals',
                'Paper & live mode with one-click switching',
                'Position sizing with risk-adjusted allocation',
                'Multi-exchange orchestration via unified API',
            ],
            techStack: ['Java 23', 'Spring Boot', 'Scheduling'],
        },
        {
            name: 'Analysis',
            icon: analysisModuleIcon,
            tagline: 'The Intelligence Engine',
            description:
                'ML-powered market analytics — LSTM price predictions, feature engineering, and GPU-accelerated model training.',
            features: [
                'LSTM neural networks for price forecasting',
                'Real-time feature engineering pipeline',
                'GPU-accelerated training with TensorFlow',
                'Automated model retraining on fresh data',
            ],
            techStack: ['Python', 'Keras', 'TensorFlow', 'GPU'],
        },
        {
            name: 'Data',
            icon: dataModuleIcon,
            tagline: 'The Market Backbone',
            description:
                'Real-time market data harvesting — live prices, historical series, sentiment signals, and internal API for every module.',
            features: [
                'Live price feeds from multiple exchanges',
                'Historical time-series with configurable granularity',
                'Sentiment ingestion from news & social feeds',
                'RESTful internal API consumed by all modules',
            ],
            techStack: ['Django', 'PostgreSQL', 'REST API'],
        },
    ]

    protected readonly analysisCodeWindow: CodeWindow = analysisCodeWindow
    protected readonly TagType: typeof TagType = TagType
    protected readonly analysisCodeSnippet: string = analysisCodeSnippet
}
