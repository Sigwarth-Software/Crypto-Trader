import * as d3 from 'd3'

import { type HistoryPoint } from '../currency/types'

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

export type SeriesType = 'line' | 'area'

export type SeriesConfig = {
    type: SeriesType
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
    data: ChartDataPoint[]
    dimensions: ChartDimensions
    series: SeriesConfig
    axes: AxisConfig
    theme: ChartTheme
}

export type ChartScales = {
    x: d3.ScaleTime<number, number>
    y: d3.ScaleLinear<number, number>
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
