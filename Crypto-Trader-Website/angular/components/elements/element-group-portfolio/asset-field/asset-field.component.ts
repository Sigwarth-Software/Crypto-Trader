// asset-field.component.ts
import {
    Component,
    EventEmitter,
    HostBinding,
    Input,
    Output,
} from '@angular/core';

import { TagType } from '@theoliverlear/angular-suite';
import { CurrencyFormatterService } from '@ui/currency-formatter.service';
import { SharesFormatterService } from '@ui/shares-formatter.service';
import { TimeFormatterService } from '@ui/time-formatter.service';
import { PortfolioAsset } from '@models/portfolio/types';
import { SimpleSortState } from '@models/sort/SimpleSortState';
import { AssetFieldSortType } from '@models/sort/types';

import { PortfolioAssetFieldType } from './models/PortfolioAssetFieldType';

@Component({
    selector: 'asset-field',
    templateUrl: './asset-field.component.html',
    styleUrls: ['./asset-field.component.scss'],
    standalone: false,
})
export class AssetFieldComponent {
    @Input() public fieldType: PortfolioAssetFieldType;
    @Input() public asset: PortfolioAsset;
    @Input() public showFieldTitle: boolean = true;
    @Input() public currentSort: AssetFieldSortType = [
        PortfolioAssetFieldType.CURRENCY_NAME,
        SimpleSortState.NONE,
    ];
    @Output() public sortClicked: EventEmitter<AssetFieldSortType> =
        new EventEmitter<AssetFieldSortType>();
    @HostBinding('class.highlighted-field') protected get isHighlighted(): boolean {
        return (
            this.fieldType === PortfolioAssetFieldType.TOTAL_VALUE ||
            this.fieldType === PortfolioAssetFieldType.SHARES
        );
    }
    constructor(
        private readonly sharesFormatter: SharesFormatterService,
        private readonly currencyFormatter: CurrencyFormatterService,
        private readonly timeFormatter: TimeFormatterService,
    ) {}

    getFieldText(): string {
        switch (this.fieldType) {
            case PortfolioAssetFieldType.CURRENCY_NAME:
                return this.getCurrencyName();
            case PortfolioAssetFieldType.SHARES:
                return this.sharesFormatter.formatShares(
                    this.asset.shares,
                    this.asset.currencyCode,
                );
            case PortfolioAssetFieldType.TARGET_PRICE:
                return this.currencyFormatter.formatCurrency(
                    this.asset.targetPrice,
                );
            case PortfolioAssetFieldType.LAST_UPDATED:
                return this.getFormattedLastUpdated();
            case PortfolioAssetFieldType.VENDOR:
                return this.asset.vendorName;
            case PortfolioAssetFieldType.TOTAL_VALUE:
                return this.currencyFormatter.formatCurrency(
                    this.asset.totalValueInDollars,
                );
            default:
                throw new Error(
                    `Unknown PortfolioAssetFieldType: ${this.fieldType}`,
                );
        }
    }

    emitSortClick(sortState: SimpleSortState): void {
        this.sortClicked.emit([
            this.fieldType,
            sortState,
        ] as AssetFieldSortType);
    }

    getCurrentIconState(): SimpleSortState {
        if (!this.currentSort) {
            return SimpleSortState.NONE;
        }
        if (this.currentSort[0] === this.fieldType) {
            return this.currentSort[1];
        } else {
            return SimpleSortState.NONE;
        }
    }

    private getCurrencyName(): string {
        return `${this.asset.currencyName} (${this.asset.currencyCode  })`;
    }

    private getFormattedLastUpdated(): string {
        return this.timeFormatter
            .formatTime(this.asset.lastTraded)
            .replace(',', '');
    }

    protected readonly TagType: typeof TagType = TagType;
}
