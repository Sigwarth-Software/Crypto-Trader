// base-chart.component.ts
import {
    AfterViewInit,
    Directive,
    ElementRef,
    Input,
    OnChanges,
    OnDestroy,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import * as d3 from 'd3';
import { Subject } from 'rxjs';

import { ChartService } from '@ui/chart.service';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import {
    type ChartConfig,
    type ChartScales,
    type ParsedPoint,
} from '@models/chart/types';

/** Abstract base for all chart components. Owns the shared SVG lifecycle
 *  (clear → dimension → scale → draw) and delegates series/label rendering
 *  to concrete subclasses.
 */
@Directive()
export abstract class BaseChartComponent
    implements OnChanges, AfterViewInit, OnDestroy
{
    @Input() public config!: ChartConfig;

    @ViewChild('svgElement', { static: true })
    protected svgRef!: ElementRef<SVGSVGElement>;

    protected readonly destroy$: Subject<void> = new Subject<void>();

    constructor(
        protected readonly engine: ChartService,
        protected readonly currencyFormatter: CurrencyFormatterService,
    ) {}

    /** Re-render when the config input changes.
     *
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('config' in changes) {
            this.render();
        }
    }

    /** Render after the view initializes.
     *
     */
    public ngAfterViewInit(): void {
        this.render();
    }

    /** Clean up subscriptions on destroy.
     *
     */
    public ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    /** Main render pipeline: clear SVG, set dimensions, parse data,
     *  create scales, then delegate to subclass hooks.
     */
    protected render(): void {
        const svg: SVGSVGElement = this.svgRef?.nativeElement;
        if (!svg) {
            return;
        }
        this.resetSVG(svg);
        this.setDimensions(svg);

        const { dimensions } = this.config;
        const width: number =
            dimensions.width - dimensions.margin.left - dimensions.margin.right;
        const height: number =
            dimensions.height - dimensions.margin.top - dimensions.margin.bottom;

        const graphic: d3.Selection<SVGGElement, unknown, null, undefined> = d3
            .select(svg)
            .append('g')
            .attr(
                'transform',
                `translate(${dimensions.margin.left},${dimensions.margin.top})`,
            );

        if (!this.config.data || this.config.data.length === 0) {
            return;
        }

        const parsed: ParsedPoint[] = this.engine.parseData(this.config.data);
        if (parsed.length === 0) {
            return;
        }

        const scales: ChartScales = this.engine.createScales(
            parsed,
            width,
            height,
        );

        this.drawSeries(graphic, parsed, scales);
        this.drawLabels(graphic, scales);
    }

    /** Draw the data series (line, area, etc.). Implemented by subclasses.
     *
     * @param graphic
     * @param data
     * @param scales
     */
    protected abstract drawSeries(
        graphic: d3.Selection<SVGGElement, unknown, null, undefined>,
        data: ParsedPoint[],
        scales: ChartScales,
    ): void;

    /** Draw axis labels (price, time, etc.). Implemented by subclasses.
     *
     * @param graphic
     * @param scales
     */
    protected abstract drawLabels(
        graphic: d3.Selection<SVGGElement, unknown, null, undefined>,
        scales: ChartScales,
    ): void;

    private setDimensions(svg: SVGSVGElement): void {
        svg.setAttribute('width', String(this.config.dimensions.width));
        svg.setAttribute('height', String(this.config.dimensions.height));
    }

    private resetSVG(svg: SVGSVGElement): void {
        while (svg.firstChild) {
            svg.removeChild(svg.firstChild);
        }
    }
}
