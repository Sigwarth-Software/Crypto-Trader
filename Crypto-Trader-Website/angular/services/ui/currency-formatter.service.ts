import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class CurrencyFormatterService {
    constructor() {}

    public formatCurrency(amount: number): string {
        if (amount == null || isNaN(amount)) {
            amount = 0;
        }
        let numDigits: number = 2;
        if (amount > 0 && amount < 1) {
            const parts = amount.toString().split('.');
            const decimalSplit: string | undefined =
                parts.length > 1 ? parts[1] : undefined;
            if (decimalSplit && decimalSplit.length > 0) {
                numDigits = decimalSplit.length;
            }
        }
        numDigits = Math.max(numDigits, 2)
        const formatter: Intl.NumberFormat = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: numDigits,
        });
        return formatter.format(amount);
    }
}
