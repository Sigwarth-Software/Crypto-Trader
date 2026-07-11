import { ChartConfig } from '@models/chart/types'

export interface ChartDisplayHandler {
    setChartData<T extends object>(chartData: T): void
    setChartConfig<T extends ChartConfig>(chartConfig: T): void
}