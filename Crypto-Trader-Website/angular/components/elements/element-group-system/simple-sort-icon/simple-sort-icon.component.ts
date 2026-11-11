// simple-sort-icon.component.ts
import {
    ChangeDetectorRef,
    Component,
    EventEmitter,
    HostListener,
    Input,
    OnChanges,
    Output,
    type SimpleChanges,
} from '@angular/core';

import { SimpleSortState } from '@models/sort/SimpleSortState';

@Component({
    selector: 'simple-sort-icon',
    templateUrl: './simple-sort-icon.component.html',
    styleUrls: ['./simple-sort-icon.component.scss'],
    standalone: false,
})
export class SimpleSortIconComponent implements OnChanges {
    @Input() public currentSortState: SimpleSortState = SimpleSortState.NONE;
    @Output() public sortStateChanged: EventEmitter<SimpleSortState> =
        new EventEmitter<SimpleSortState>();
    constructor(private readonly changeDetector: ChangeDetectorRef) {}

    public ngOnChanges(simpleChanges: SimpleChanges): void {
        if ('currentSortState' in simpleChanges) {
            this.currentSortState = simpleChanges.currentSortState.currentValue;
        }
    }

    protected getStateText(): string {
        return this.currentSortState;
    }

    @HostListener('click')
    public onClick(): void {
        this.nextSortState();
        this.changeDetector.detectChanges();
        this.emitSortState();
    }

    public nextSortState(): void {
        this.currentSortState = SimpleSortState.getNextState(this.currentSortState);
    }

    protected emitSortState(): void {
        this.sortStateChanged.emit(this.currentSortState);
    }
}
