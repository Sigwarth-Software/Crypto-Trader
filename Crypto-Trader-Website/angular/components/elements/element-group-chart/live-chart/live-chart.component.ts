import { Component, ElementRef, Input, OnInit } from '@angular/core'

import { ChartType } from './models/ChartType'

/** A live chart that updates in real time.
 *
 */
@Component({
    selector: 'live-chart',
    standalone: false,
    templateUrl: './live-chart.component.html',
    styleUrls: ['./live-chart.component.scss'],
})
export class LiveChartComponent<ChartSchema> implements OnInit {
    protected chart: ChartSchema | null = null
    @Input() public chartType: ChartType
    constructor(private readonly element: ElementRef) {}
    /** Loads the chart.
     *
     */
    public ngOnInit(): void {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const canvas: CanvasRenderingContext2D | null = (this.element.nativeElement as HTMLElement)
            .querySelector('canvas')
            ?.getContext('2d') as CanvasRenderingContext2D
    }
}
