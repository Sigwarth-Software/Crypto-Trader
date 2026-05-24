import {
    type AxisConfig,
    type ChartConfig,
    type ChartDisplayProperties,
    type ChartDimensions,
    type ChartTheme,
    type SeriesConfig,
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
    type: 'line',
    stroke: '#ffffff',
    strokeWidth: 2.0,
}

export const defaultSparklineSeries: SeriesConfig = {
    type: 'line',
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
    data: [{ date: new Date(), value: 0 }],
    dimensions: { ...defaultDimensions },
    series: { ...defaultLineSeries },
    axes: { ...defaultAxes },
    theme: { ...DASHBOARD_THEME },
}

export const defaultSparklineConfig: ChartConfig = {
    data: [],
    dimensions: { ...defaultSparklineDimensions },
    series: { ...defaultSparklineSeries },
    axes: { ...sparklineAxes },
    theme: { ...SPARK_THEME },
}

/** @deprecated Use {@link defaultChartConfig} instead. */
export const defaultChartProperties: ChartDisplayProperties = {
    data: [{ date: new Date(), value: 0 }],
    width: 500,
    height: 175,
    margin: {
        top: 20,
        right: 20,
        bottom: 30,
        left: 40,
    },
    stroke: '#ffffff',
    strokeWidth: 2.0,
    textColor: '#979797',
}
