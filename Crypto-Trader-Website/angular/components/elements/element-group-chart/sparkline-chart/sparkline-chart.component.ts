// sparkline-chart.component.ts
import { Selection } from 'd3'
import { Component } from '@angular/core'
import { BaseChartComponent } from '@components/elements/element-group-chart/base-chart/base-chart.component'
import { ChartService } from '@ui/chart.service'
import { CurrencyFormatterService } from '@ui/currency-formatter.service'
import { type ChartScales, type ParsedPoint } from '@models/chart/types'

/** A minimal sparkline chart showing a line with price min/max labels.
 *  No time axis labels. Extends the shared base chart lifecycle.
 */
@Component({
    selector: 'sparkline-chart',
    templateUrl: './sparkline-chart.component.html',
    styleUrls: ['./sparkline-chart.component.scss'],
    standalone: false,
})
export class SparklineComponent extends BaseChartComponent {
    constructor(engine: ChartService, currencyFormatter: CurrencyFormatterService) {
        super(engine, currencyFormatter)
    }

    /** Draw the sparkline path.
     *
     * @param graphic
     * @param data
     * @param scales
     */
    protected drawSeries(
        graphic: Selection<SVGGElement, unknown, null, undefined>,
        data: ParsedPoint[],
        scales: ChartScales,
    ): void {
        const pathData: string = this.engine.createLinePath(data, scales)
        graphic
            .append('path')
            .attr('fill', 'none')
            .attr('stroke', this.config.series.stroke)
            .attr('stroke-width', this.config.series.strokeWidth)
            .attr('d', pathData)
    }

    /** Draw price min/max labels only (no time labels).
     *
     * @param graphic
     * @param scales
     */
    protected drawLabels(
        graphic: Selection<SVGGElement, unknown, null, undefined>,
        scales: ChartScales,
    ): void {
        if (!this.config.axes.showPriceLabels) {
            return
        }
        const { theme, axes } = this.config
        const [yMin, yMax] = scales.y.domain()
        const labelPadTopEm: number = 1.4
        const labelPadBottomEm: number = -0.5

        graphic
            .append('text')
            .attr('x', scales.width)
            .attr('y', scales.y(yMax))
            .attr('dx', '-6')
            .attr('dy', `${labelPadTopEm}em`)
            .attr('text-anchor', 'end')
            .attr('fill', theme.textColor)
            .attr('font-size', axes.fontSize)
            .text(this.currencyFormatter.formatCurrency(yMax))

        graphic
            .append('text')
            .attr('x', scales.width)
            .attr('y', scales.y(yMin))
            .attr('dx', '-6')
            .attr('dy', `-${labelPadBottomEm}em`)
            .attr('text-anchor', 'end')
            .attr('fill', theme.textColor)
            .attr('font-size', axes.fontSize)
            .text(this.currencyFormatter.formatCurrency(yMin))
    }
}
