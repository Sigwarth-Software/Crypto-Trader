// chart-engine.service.ts
import { Injectable } from '@angular/core'
import * as d3 from 'd3'

import { type ChartDataPoint, type ChartScales, type ParsedPoint } from '@models/chart/types'

/** A service that encapsulates D3 chart logic, providing scale creation,
 *  line/area path generation, and axis formatting.
 */
@Injectable({ providedIn: 'root' })
export class ChartService {
    /**
     * Parse raw chart data points into chronological date/value pairs,
     *  filtering invalid entries and consolidating duplicate timestamps.
     *
     * @param data
     * @returns The parsed data points.
     */
    public parseData(data: ChartDataPoint[]): ParsedPoint[] {
        const pointsByTimestamp: Map<number, ParsedPoint> = new Map<number, ParsedPoint>()
        data.map(
            (point: ChartDataPoint): ParsedPoint => ({
                date: point.date instanceof Date ? point.date : new Date(point.date),
                value: point.value,
            }),
        )
            .filter(
                (point: ParsedPoint): boolean =>
                    !isNaN(point.date.getTime()) && Number.isFinite(point.value),
            )
            .forEach((point: ParsedPoint): void => {
                pointsByTimestamp.set(point.date.getTime(), point)
            })

        return Array.from(pointsByTimestamp.values()).sort(
            (left: ParsedPoint, right: ParsedPoint): number =>
                left.date.getTime() - right.date.getTime(),
        )
    }

    /**
     * Create D3 time and linear scales from parsed data points.
     *
     * @param data
     * @param width
     * @param height
     * @returns The chart scales.
     */
    public createScales(data: ParsedPoint[], width: number, height: number): ChartScales {
        const x: d3.ScaleTime<number, number> = d3
            .scaleTime()
            .domain(d3.extent(data, (point: ParsedPoint): Date => point.date) as [Date, Date])
            .range([0, width])
        const y: d3.ScaleLinear<number, number> = d3
            .scaleLinear()
            .domain(
                d3.extent(data, (point: ParsedPoint): number => point.value) as [number, number],
            )
            .nice()
            .range([height, 0])
        return { x, y, width, height }
    }

    /**
     * Generate an SVG path string for a line series.
     *
     * @param data
     * @param scales
     * @returns The SVG path string for the line series.
     */
    public createLinePath(data: ParsedPoint[], scales: ChartScales): string {
        const lineGenerator: d3.Line<ParsedPoint> = d3
            .line<ParsedPoint>()
            .x((point: ParsedPoint): number => scales.x(point.date))
            .y((point: ParsedPoint): number => scales.y(point.value))
            .defined((point: ParsedPoint): boolean => Number.isFinite(point.value))
        return lineGenerator(data) ?? ''
    }

    /**
     * Generate an SVG path string for an area series.
     *
     * @param data
     * @param scales
     * @returns The SVG path string for the area series.
     */
    public createAreaPath(data: ParsedPoint[], scales: ChartScales): string {
        const areaGenerator: d3.Area<ParsedPoint> = d3
            .area<ParsedPoint>()
            .x((point: ParsedPoint): number => scales.x(point.date))
            .y0(scales.height)
            .y1((point: ParsedPoint): number => scales.y(point.value))
            .defined((point: ParsedPoint): boolean => Number.isFinite(point.value))
        return areaGenerator(data) ?? ''
    }

    // TODO: Return needs a type.
    /**
     * Format time domain boundaries for axis labels, ensuring
     *  chronological order and flooring to the nearest minute.
     *
     * @param scales
     * @param formatString
     * @returns The starting and ending label for a scale.
     */
    public formatTimeBounds(
        scales: ChartScales,
        formatString: string,
    ): { startLabel: string; endLabel: string } {
        let [domainStart, domainEnd]: Date[] = scales.x.domain() as [Date, Date]
        if (domainStart > domainEnd) {
            const tmp: Date = domainStart
            domainStart = domainEnd
            domainEnd = tmp
        }
        // TODO: Add a datetime library. This logic is hard to read, and unnecessary.
        const startTime: Date = new Date(Math.floor(domainStart.getTime() / 60000) * 60000)
        const endTime: Date = new Date(Math.floor(domainEnd.getTime() / 60000) * 60000)
        // TODO: Extract datetime function - TimeFormatFunctionD3.
        const formatTime: { (date: Date): string } = d3.timeFormat(formatString)
        return {
            startLabel: formatTime(startTime),
            endLabel: formatTime(endTime),
        }
    }
}
