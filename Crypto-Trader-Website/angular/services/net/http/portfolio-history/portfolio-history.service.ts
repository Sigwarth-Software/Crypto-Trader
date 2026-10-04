import { Injectable } from '@angular/core'
import { HttpClientService } from '@theoliverlear/angular-suite'
import { PortfolioHistory } from '@models/portfolio/types'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

@Injectable({
  providedIn: 'root'
})
export class PortfolioHistoryService extends HttpClientService<never, PortfolioHistory[]> {
    private static readonly URL: string = `${environment.apiUrl}/portfolio/history/get`
    constructor() {
        super(PortfolioHistoryService.URL)
    }

    getPortfolioHistory(): Observable<PortfolioHistory[]> {
        return this.get()
    }
}