import {
    type AxisConfig,
    type ChartConfig,
    type ChartDimensions,
    type ChartTheme,
    type SeriesConfig,
    ChartSeriesType,
} from '@models/chart/types'

export const DASHBOARD_THEME: ChartTheme = {
    textColor: '#979797',
    gridColor: '#3c3c3c',
    backgroundColor: 'transparent',
    fontFamily: 'inherit',
    fontSize: 10,
}

export const SPARK_THEME: ChartTheme = {
    textColor: '#9aa0a6',
    gridColor: '#3c3c3c',
    backgroundColor: 'transparent',
    fontFamily: 'inherit',
    fontSize: 10,
}

export const defaultLineSeries: SeriesConfig = {
    type: ChartSeriesType.Line,
    stroke: '#ffffff',
    strokeWidth: 2.0,
}

export const defaultSparklineSeries: SeriesConfig = {
    type: ChartSeriesType.Line,
    stroke: '#4caf50',
    strokeWidth: 2.0,
}

export const defaultDimensions: ChartDimensions = {
    width: 500,
    height: 175,
    margin: { top: 20, right: 20, bottom: 30, left: 40 },
}

export const defaultSparklineDimensions: ChartDimensions = {
    width: 120,
    height: 40,
    margin: { top: 20, right: 20, bottom: 20, left: 20 },
}

export const defaultAxes: AxisConfig = {
    showTimeLabels: true,
    showPriceLabels: true,
    timeFormat: '%m-%d-%y, %-I%p',
    fontSize: 10,
}

export const sparklineAxes: AxisConfig = {
    showTimeLabels: false,
    showPriceLabels: true,
    timeFormat: '%m-%d-%y, %-I%p',
    fontSize: 10,
}

export const defaultChartConfig: ChartConfig = {
    dimensions: { ...defaultDimensions },
    series: { ...defaultLineSeries },
    axes: { ...defaultAxes },
    theme: { ...DASHBOARD_THEME },
}

export const defaultSparklineConfig: ChartConfig = {
    dimensions: { ...defaultSparklineDimensions },
    series: { ...defaultSparklineSeries },
    axes: { ...sparklineAxes },
    theme: { ...SPARK_THEME },
}

export function createChartConfig(config: Partial<ChartConfig> = {}): ChartConfig {
    return { ...defaultChartConfig, ...config }
}

export function createSparklineConfig(config: Partial<ChartConfig> = {}): ChartConfig {
    return { ...defaultSparklineConfig, ...config }
}