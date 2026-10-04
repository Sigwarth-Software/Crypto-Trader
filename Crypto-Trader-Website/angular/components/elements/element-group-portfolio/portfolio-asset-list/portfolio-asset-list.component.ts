// portfolio-asset-list.component.ts
import { Component, Input, OnInit } from '@angular/core';

import { PortfolioAsset } from '@models/portfolio/types';
import { SimpleSortState } from '@models/sort/SimpleSortState';
import { AssetFieldSortType } from '@models/sort/types';

import { PortfolioAssetFieldType } from '../asset-field/models/PortfolioAssetFieldType';

/**
 * Component for displaying a list of assets in a portfolio.
 */
@Component({
    selector: 'portfolio-asset-list',
    templateUrl: './portfolio-asset-list.component.html',
    styleUrls: ['./portfolio-asset-list.component.scss'],
    standalone: false,
})
export class PortfolioAssetListComponent implements OnInit {
    @Input() public assets: PortfolioAsset[] = []
    private originalAssetsState: PortfolioAsset[] = []
    public sortState: AssetFieldSortType = [
        PortfolioAssetFieldType.CURRENCY_NAME,
        SimpleSortState.NONE,
    ]
    constructor() {}

    /**
     * On init, try to store original assets.
     */
    public ngOnInit(): void {
        this.attemptStoreOriginalAssets()
    }

    private attemptStoreOriginalAssets(): void {
        if (this.hasAssets()) {
            this.originalAssetsState = [...this.assets]
        }
    }

    /** Checks if assets list has assets.
     * @returns true if assets list has assets, false otherwise.
     */
    public hasAssets(): boolean {
        return this.assets.length > 0
    }

    /** Sorts assets list by type.
     *
     * @param assetSortType
     */
    public sortAssets(assetSortType: AssetFieldSortType): void {
        if (!this.canSort()) {
            return
        }
        this.sortState = assetSortType
        if (assetSortType[1] === SimpleSortState.NONE) {
            this.resetSort()
            return
        }
        this.sortByType(assetSortType)
    }

    private sortByType(assetSortType: AssetFieldSortType): void {
        switch (assetSortType[0]) {
            case PortfolioAssetFieldType.TOTAL_VALUE:
                this.sortByTotalValue()
                break
            case PortfolioAssetFieldType.CURRENCY_NAME:
                this.sortByCurrencyName()
                break
            case PortfolioAssetFieldType.SHARES:
                this.sortByShares()
                break
            case PortfolioAssetFieldType.LAST_UPDATED:
                this.sortByLastUpdated()
                break
            case PortfolioAssetFieldType.TARGET_PRICE:
                this.sortByTargetPrice()
                break
            case PortfolioAssetFieldType.VENDOR_NAME:
                this.sortByVendorName()
                break
            default:
                throw new Error(`Unknown AssetFieldSortType: ${assetSortType.toString()}`)
        }
    }

    protected canSort(): boolean {
        return this.assets.length > 1
    }

    protected sortByCurrencyName(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.currencyName.localeCompare(b.currencyName))
        } else {
            this.assets.sort((a, b): number => b.currencyName.localeCompare(a.currencyName))
        }
    }

    protected sortByShares(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.shares - b.shares)
        } else {
            this.assets.sort((a, b): number => b.shares - a.shares)
        }
    }

    private sortByTotalValue(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.totalValueInDollars - b.totalValueInDollars)
        } else {
            this.assets.sort((a, b): number => b.totalValueInDollars - a.totalValueInDollars)
        }
    }

    private sortByTargetPrice(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.targetPrice - b.targetPrice)
        } else {
            this.assets.sort((a, b): number => b.targetPrice - a.targetPrice)
        }
    }

    private sortByVendorName(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.vendorName.localeCompare(b.vendorName))
        } else {
            this.assets.sort((a, b): number => b.vendorName.localeCompare(a.vendorName))
        }
    }

    private sortByLastUpdated(): void {
        const sortType: SimpleSortState = this.sortState[1]
        if (sortType === SimpleSortState.ASCENDING) {
            this.assets.sort((a, b): number => a.lastUpdated.localeCompare(b.lastUpdated))
        } else {
            this.assets.sort((a, b): number => b.lastUpdated.localeCompare(a.lastUpdated))
        }
    }

    private resetSort(): void {
        this.assets = [...this.originalAssetsState]
    }
}
