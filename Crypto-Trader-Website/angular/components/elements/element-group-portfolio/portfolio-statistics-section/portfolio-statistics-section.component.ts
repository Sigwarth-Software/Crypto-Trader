// portfolio-statistics-section.component.ts
import { Component } from '@angular/core';

import { createSparklineConfig } from '@assets/chart.assets'
import { type ChartConfig } from '@models/chart/types';

@Component({
    selector: 'portfolio-stats-section',
    templateUrl: './portfolio-statistics-section.component.html',
    styleUrls: ['./portfolio-statistics-section.component.scss'],
    standalone: false,
})
export class PortfolioStatisticsSectionComponent {
    protected sparklineConfig: ChartConfig = createSparklineConfig()

    constructor() {}
}
