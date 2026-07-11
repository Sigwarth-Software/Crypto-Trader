/**
 * Takes an ISO date-time string and formats it into a more human-readable format.
 * The output format is "HH:MM:SSAM/PM YYYY-MM-DD".
 *
 * @param isoDateTime - The ISO date-time string to format.
 * @returns A formatted date-time string.
 */
export function formatIsoDateTime(isoDateTime: string): string {
    const date: Date = new Date(isoDateTime)
    const year: number = date.getFullYear()
    const month: string = String(date.getMonth() + 1).padStart(2, '0')
    const day: string = String(date.getDate()).padStart(2, '0')
    let dayHours: number = date.getHours()
    let dayPeriod: string = 'AM'
    if (dayHours >= 12) {
        dayPeriod = 'PM'
        dayHours -= 12
    }
    const hours: string = String(dayHours).padStart(2, '0')
    const minutes: string = String(date.getMinutes()).padStart(2, '0')
    const seconds: string = String(date.getSeconds()).padStart(2, '0')
    return `${hours}:${minutes}:${seconds}${dayPeriod} ${year}-${month}-${day}`
}