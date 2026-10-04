// search-input.component.ts
import { Component, EventEmitter, HostBinding, Input, OnInit, Output } from '@angular/core'
import { FormControl } from '@angular/forms'
import { map, Observable, startWith } from 'rxjs'

import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'

// TODO: This can be made generic to support any type of input, not just
//       strings.
/**
 * A search input that filters options.
 */
@Component({
    selector: 'search-input',
    templateUrl: './search-input.component.html',
    styleUrls: ['./search-input.component.scss'],
    standalone: false,
})
export class SearchInputComponent implements OnInit {
    protected searchControl: FormControl<string | null> = new FormControl<string | null>('')
    @Input() public options: string[] = []
    protected filteredOptions: Observable<string[]> = this.searchControl.valueChanges.pipe(
        startWith(''),
        map((value: string | null): string[] => this.filter(value || '')),
    )
    @Input() public isEnabled: boolean = true

    /** Whether the input is disabled.
     * @returns If the input is disabled.
     */
    @HostBinding('class.disabled') protected get disabled(): boolean {
        return !this.isEnabled
    }

    @Output() public selectionChange: EventEmitter<string> = new EventEmitter<string>()
    constructor(private readonly logger: CryptoTraderLoggerService) {}

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.System)
    }

    /**
     * Emits the selection change event.
     */
    protected emitSelectionChange(): void {
        if (!this.searchControl.value) {
            return
        }
        this.logger.info(`Search selection made: ${this.searchControl.value}`)
        this.selectionChange.emit(this.searchControl.value)
    }

    /**
     * Clears the search input.
     */
    protected clear(): void {
        this.logger.debug('Clearing search input')
        this.searchControl.setValue('')
    }

    private filter(value: string | null): string[] {
        if (!value) {
            return []
        }
        const filterValue: string = value.toLowerCase()
        return this.options.filter((option: string): boolean =>
            option.toLowerCase().includes(filterValue),
        )
    }
}
