// asset-field-list.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { PortfolioAsset } from '@models/portfolio/types';
import { AssetFieldSortType } from '@models/sort/types';

import { PortfolioAssetFieldType } from '../asset-field/models/PortfolioAssetFieldType';

@Component({
    selector: 'asset-field-list',
    templateUrl: './asset-field-list.component.html',
    styleUrls: ['./asset-field-list.component.scss'],
    standalone: false,
})
export class AssetFieldListComponent {
    // TODO: Put repeated default PortfolioAsset in assets file.
    @Input() public asset: PortfolioAsset = {
        id: 0,
        currencyName: '',
        currencyCode: '',
        shares: 0,
        sharesValueInDollars: 0,
        assetWalletDollars: 0,
        totalValueInDollars: 0,
        targetPrice: 0,
        lastTraded: '',
        vendorName: '',
    };
    @Input() public showFieldTitles: boolean = true;
    @Input() public currentSort: AssetFieldSortType;
    @Output() public sortClicked: EventEmitter<AssetFieldSortType> =
        new EventEmitter<AssetFieldSortType>();
    constructor() {}

    public emitSortClick(sortType: AssetFieldSortType): void {
        this.sortClicked.emit(sortType);
    }

    protected readonly PortfolioAssetFieldType: typeof PortfolioAssetFieldType = PortfolioAssetFieldType;
}
