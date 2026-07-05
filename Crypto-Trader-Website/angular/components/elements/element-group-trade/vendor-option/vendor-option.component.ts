// vendor-option.component.ts
import {
    Component,
    EventEmitter,
    HostBinding,
    HostListener,
    Input,
    Output,
} from '@angular/core';

import { TagType } from '@theoliverlear/angular-suite';
import { starIcon } from '@assets/image.assets';
import { VendorOption } from '@models/vendor/VendorOption';

@Component({
    selector: 'vendor-option',
    templateUrl: './vendor-option.component.html',
    styleUrls: ['./vendor-option.component.scss'],
    standalone: false,
})
export class VendorOptionComponent {
    @Input() vendorOption: VendorOption;
    @Input() isRecommended: boolean = true;
    @Input() isSelected: boolean = false;
    @Input() selectedVendor: VendorOption;
    @Output() vendorSelectedEvent: EventEmitter<VendorOption> =
        new EventEmitter<VendorOption>();
    @HostBinding('class.selected') get selected(): boolean {
        return this.selectedVendor === this.vendorOption;
    }
    constructor() {}

    setSelected(vendorOption: VendorOption): void {
        this.isSelected = this.vendorOption === vendorOption;
    }

    @HostListener('click')
    onClick() {
        this.vendorSelectedEvent.emit(this.vendorOption);
    }

    protected readonly VendorOption = VendorOption;
    protected readonly starIcon = starIcon;
    protected readonly TagType = TagType;
}
