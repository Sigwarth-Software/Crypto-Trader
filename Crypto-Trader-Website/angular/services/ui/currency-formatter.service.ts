import { Injectable } from '@angular/core'

/**
 * A service that formats currency values.
 */
@Injectable({
    providedIn: 'root',
})
export class CurrencyFormatterService {
    constructor() {}

    /**
     * Formats a currency value as a string in USD format.
     *
     * @param amount The currency value to format.
     * @param limitTwoDigits Whether to force the formatted value to have at
     * most two decimal places.
     *
     * @returns The formatted currency string.
     */
    // TODO: Replace flag with params object.
    public formatCurrency(amount: number, limitTwoDigits: boolean = false): string {
        if (isNaN(amount)) {
            amount = 0
        }
        let numDigits: number = 2
        if (amount > 0 && amount < 1) {
            const parts: string[] = amount.toString().split('.')
            const decimalSplit: string | undefined = parts.length > 1 ? parts[1] : undefined
            if (decimalSplit && decimalSplit.length > 0) {
                numDigits = decimalSplit.length
            }
        }
        if (limitTwoDigits) {
            numDigits = 2
        }
        numDigits = Math.max(numDigits, 2)
        const formatter: Intl.NumberFormat = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: numDigits,
        })
        return formatter.format(amount)
    }
}
