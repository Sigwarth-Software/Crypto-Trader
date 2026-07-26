// portfolio-asset.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core'

import { PortfolioAsset } from '@models/portfolio/types'
import { AssetFieldSortType } from '@models/sort/types'

/**
 * A component that displays a portfolio asset.
 */
@Component({
    selector: 'portfolio-asset',
    templateUrl: './portfolio-asset.component.html',
    styleUrls: ['./portfolio-asset.component.scss'],
    standalone: false,
})
export class PortfolioAssetComponent {
    // TODO: Extract a default asset.
    @Input() public asset: PortfolioAsset = {
        id: 0,
        currencyName: '',
        currencyCode: '',
        shares: 0,
        sharesValueInDollars: 0,
        assetWalletDollars: 0,
        totalValueInDollars: 0,
        targetPrice: 0,
        lastUpdated: '',
        vendorName: '',
    }
    @Input() public isFirst: boolean = false
    @Input() public currentSort: AssetFieldSortType
    @Output() public sortClicked: EventEmitter<AssetFieldSortType> =
        new EventEmitter<AssetFieldSortType>()
    constructor() {}

    protected emitSortClick(sortType: AssetFieldSortType): void {
        this.sortClicked.emit(sortType)
    }
}
