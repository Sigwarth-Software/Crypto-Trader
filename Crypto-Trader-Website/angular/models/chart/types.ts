import { type HistoryPoint } from '../currency/types'
import { ScaleLinear, ScaleTime } from 'd3'

export type SparkPoint = {
    date: Date | string | number
    value: number
}

export type Margin = {
    top: number
    right: number
    bottom: number
    left: number
}

export type ChartDataPoint = SparkPoint | HistoryPoint

export enum ChartSeriesType {
    Line = 'line',
    Area = 'area',
}

export type SeriesConfig = {
    type: ChartSeriesType
    stroke: string
    strokeWidth: number
    fill?: string
}

export type AxisConfig = {
    showTimeLabels: boolean
    showPriceLabels: boolean
    timeFormat: string
    fontSize: number
}

export type ChartTheme = {
    textColor: string
    gridColor: string
    backgroundColor: string
    fontFamily: string
    fontSize: number
}

export type ChartDimensions = {
    width: number
    height: number
    margin: Margin
}

export type ChartConfig = {
    dimensions: ChartDimensions
    series: SeriesConfig
    axes: AxisConfig
    theme: ChartTheme
}

export type ChartScales = {
    x: ScaleTime<number, number>
    y: ScaleLinear<number, number>
    width: number
    height: number
}

export type ParsedPoint = {
    date: Date
    value: number
}

/** @deprecated Use {@link ChartConfig} instead. */
export type ChartDisplayProperties = {
    data: SparkPoint[] | HistoryPoint[]
    width: number
    height: number
    stroke: string
    margin: Margin
    strokeWidth: number
    textColor: string
}
