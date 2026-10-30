import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class TimeFormatterService {
    constructor() {}

    public formatTime(time: string, showSeconds: boolean = false): string {
        if (!time) {
            return '';
        }
        const date: Date = new Date(time);
        if (isNaN(date.getTime())) {
            return time;
        }

        return new Intl.DateTimeFormat('default', {
            year: '2-digit',
            month: 'numeric',
            day: 'numeric',
            hour: 'numeric',
            minute: 'numeric',
            second: showSeconds ? 'numeric' : undefined,
        }).format(date);
    }
}
