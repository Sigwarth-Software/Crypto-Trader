import { PortfolioHistory, RangedPortfolioHistoryRequest } from '@models/portfolio/types'
import { environment } from '@environments/environment'
import { Injectable } from '@angular/core'
import { HttpClientService } from '@theoliverlear/angular-suite'
import { Observable } from 'rxjs'

/**
 * Fetches the portfolio history for a given date range from the backend API.
 */
@Injectable({
    providedIn: 'root',
})
export class RangedPortfolioHistoryService extends HttpClientService<
    RangedPortfolioHistoryRequest,
    PortfolioHistory[]
> {
    private static readonly URL: string = `${environment.apiUrl}/portfolio/history/ranged/get`

    constructor() {
        super(RangedPortfolioHistoryService.URL)
    }

    /**
     * POSTs a request to the backend API to fetch the portfolio history for a given date range.
     * @param request The request date range.
     * @returns An observable that emits the portfolio history data.
     */
    public getRangedPortfolioHistory(
        request: RangedPortfolioHistoryRequest,
    ): Observable<PortfolioHistory[]> {
        return this.post(request)
    }
}
