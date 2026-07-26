// portfolio-chart.component.ts
import {
    AfterViewInit,
    Component,
    ElementRef,
    Input,
    OnChanges,
    SimpleChanges,
    ViewChild,
} from '@angular/core'
import { Selection } from 'd3'
import * as d3 from 'd3'

import { CurrencyFormatterService } from '@ui/currency-formatter.service'
import { Margin, SparkPoint } from '@models/chart/types'

/**
 * A chart showcasing portfolio data.
 */
@Component({
    selector: 'portfolio-chart',
    templateUrl: './portfolio-chart.component.html',
    styleUrls: ['./portfolio-chart.component.scss'],
    standalone: false,
})
export class PortfolioChartComponent implements OnChanges, AfterViewInit {
    @Input() public data: SparkPoint[] = []
    @Input() public width: number = 120
    @Input() public height: number = 40
    @Input() public stroke: string = '#4caf50'
    @Input() public strokeWidth: number = 2.0
    @Input() public margin: Margin = { top: 20, right: 20, bottom: 20, left: 20 }

    @ViewChild('svgElement', { static: true })
    protected readonly chartReference!: ElementRef<SVGSVGElement>
    constructor(private readonly currencyFormatter: CurrencyFormatterService) {}

    /**
     * On changes, render the chart with new data.
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('data' in changes) {
            this.render()
        }
    }

    /**
     * After the view is created, load the data.
     */
    public ngAfterViewInit(): void {
        this.render()
    }

    private render(): void {
        // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
        const svgElement: SVGSVGElement = this.chartReference?.nativeElement
        // eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
        if (!svgElement) {
            return
        }

        const width: number = this.getAdjustedWidth()
        const height: number = this.getAdjustedHeight()
        this.resetSVG(svgElement)
        this.setDimensions(svgElement)

        const graphic: Selection<SVGGElement, unknown, null, undefined> = d3
            .select(svgElement)
            .append('g')
            .attr('transform', `translate(${this.margin.left},${this.margin.top})`)

        if (!this.data || this.data.length === 0) {
            return
        }

        const parsed: { date: Date; value: number }[] = this.data
            .map((point) => ({
                date: point.date instanceof Date ? point.date : new Date(point.date),
                value: point.value,
            }))
            .filter((date) => !isNaN(date.date.getTime()))
        if (parsed.length === 0) {
            return
        }

        const xAxis: any = d3
            .scaleTime()
            .domain(d3.extent(parsed, (d: { date: Date; value: number }) => d.date) as [Date, Date])
            .range([0, width])
        const yAxis: any = d3
            .scaleLinear()
            .domain(
                d3.extent(parsed, (d: { date: Date; value: number }) => d.value) as [
                    number,
                    number,
                ],
            )
            .nice()
            .range([height, 0])

        const line = d3
            .line()
            .x((d: any) => xAxis(d.date))
            .y((d: any) => yAxis(d.value))
            .defined((d: any) => Number.isFinite(d.value))

        graphic
            .append('path')
            .datum(parsed as any)
            .attr('fill', 'none')
            .attr('stroke', this.stroke)
            .attr('stroke-width', this.strokeWidth)
            .attr('d', line as any)

        const [yMin, yMax] = yAxis.domain()
        const labelPadTopEm: number = 1.4
        const labelPadBottomEm: number = -0.5

        graphic
            .append('text')
            .attr('x', width)
            .attr('y', yAxis(yMax))
            .attr('dx', '-6')
            .attr('dy', `${labelPadTopEm}em`)
            .attr('text-anchor', 'end')
            .attr('fill', '#9aa0a6')
            .attr('font-size', 10)
            .text(this.currencyFormatter.formatCurrency(yMax))

        graphic
            .append('text')
            .attr('x', width)
            .attr('y', yAxis(yMin))
            .attr('dx', '-6')
            .attr('dy', `-${labelPadBottomEm}em`)
            .attr('text-anchor', 'end')
            .attr('fill', '#9aa0a6')
            .attr('font-size', 10)
            .text(this.currencyFormatter.formatCurrency(yMin))
    }

    private setDimensions(svgElement: SVGSVGElement): void {
        svgElement.setAttribute('width', String(this.width))
        svgElement.setAttribute('height', String(this.height))
    }

    private resetSVG(svgElement: SVGSVGElement): void {
        while (svgElement.firstChild) {
            svgElement.removeChild(svgElement.firstChild)
        }
    }

    private getAdjustedHeight(): number {
        return this.height - this.margin.top - this.margin.bottom
    }

    private getAdjustedWidth(): number {
        return this.width - this.margin.left - this.margin.right
    }
}
