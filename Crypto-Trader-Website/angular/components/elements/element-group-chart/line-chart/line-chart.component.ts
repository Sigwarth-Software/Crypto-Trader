// line-chart.component.ts
import { Component, HostListener } from '@angular/core';
import * as d3 from 'd3';

import { BaseChartComponent } from '@components/elements/element-group-chart/base-chart/base-chart.component';
import { ChartService } from '@ui/chart.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { PixelCalculatorService } from '@ui/pixel-calculator.service';
import { type ChartScales, type ParsedPoint } from '@models/chart/types';

/** A line chart component with time and price labels. Extends the shared
 *  base chart lifecycle and delegates D3 logic to the engine service.
 */
@Component({
    selector: 'line-chart',
    standalone: false,
    templateUrl: './line-chart.component.html',
    styleUrls: ['./line-chart.component.scss'],
})
export class LineChartComponent extends BaseChartComponent {
    constructor(
        engine: ChartService,
        currencyFormatter: CurrencyFormatterService,
        private readonly pixelCalculator: PixelCalculatorService,
    ) {
        super(engine, currencyFormatter);
    }

    /** Resize the chart when the window is resized.
     *
     * @param event
     */
    @HostListener('window:resize', ['$event'])
    public onResize(event: Event): void {
        this.render();
    }

    /** Override render to dynamically size based on viewport before
     *  delegating to the base pipeline.
     */
    protected override render(): void {
        let widthVw = 40
        if (window.innerWidth < 1100) {
            widthVw = 80
        }
        this.config.dimensions.width = this.pixelCalculator.getByViewport(widthVw, 0);
        this.config.dimensions.height = this.pixelCalculator.getByViewport(0, 19);
        super.render();
    }

    /** Draw the line series path.
     *
     * @param graphic
     * @param data
     * @param scales
     */
    protected drawSeries(
        graphic: d3.Selection<SVGGElement, unknown, null, undefined>,
        data: ParsedPoint[],
        scales: ChartScales,
    ): void {
        const pathData: string = this.engine.createLinePath(data, scales);
        graphic
            .append('path')
            .attr('fill', 'none')
            .attr('stroke', this.config.series.stroke)
            .attr('stroke-width', this.config.series.strokeWidth)
            .attr('d', pathData);
    }

    /** Draw price labels at min/max and time labels at start/end.
     *
     * @param graphic
     * @param scales
     */
    protected drawLabels(
        graphic: d3.Selection<SVGGElement, unknown, null, undefined>,
        scales: ChartScales,
    ): void {
        const { theme, axes } = this.config;
        const [yMin, yMax] = scales.y.domain();
        const labelPadTopEm: number = 1.4;
        const labelPadBottomEm: number = -0.5;

        if (axes.showPriceLabels) {
            graphic
                .append('text')
                .attr('x', scales.width)
                .attr('y', scales.y(yMax))
                .attr('dx', '-6')
                .attr('dy', `${labelPadTopEm}em`)
                .attr('text-anchor', 'end')
                .attr('fill', theme.textColor)
                .attr('font-size', axes.fontSize)
                .text(this.currencyFormatter.formatCurrency(yMax));

            graphic
                .append('text')
                .attr('x', scales.width)
                .attr('y', scales.y(yMin))
                .attr('dx', '-6')
                .attr('dy', `-${labelPadBottomEm}em`)
                .attr('text-anchor', 'end')
                .attr('fill', theme.textColor)
                .attr('font-size', axes.fontSize)
                .text(this.currencyFormatter.formatCurrency(yMin));
        }

        if (axes.showTimeLabels) {
            const { startLabel, endLabel } = this.engine.formatTimeBounds(
                scales,
                axes.timeFormat,
            );

            graphic
                .append('text')
                .attr('x', 0)
                .attr('y', scales.height)
                .attr('dx', '6')
                .attr('dy', '1.6em')
                .attr('text-anchor', 'start')
                .attr('fill', theme.textColor)
                .attr('font-size', axes.fontSize)
                .text(startLabel);

            graphic
                .append('text')
                .attr('x', scales.width)
                .attr('y', scales.height)
                .attr('dx', '-6')
                .attr('dy', '1.6em')
                .attr('text-anchor', 'end')
                .attr('fill', theme.textColor)
                .attr('font-size', axes.fontSize)
                .text(endLabel);
        }
    }
}
