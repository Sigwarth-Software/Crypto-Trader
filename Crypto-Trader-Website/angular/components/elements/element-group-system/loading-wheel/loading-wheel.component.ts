// loading-wheel.component.ts
import { Component, HostBinding, Input } from '@angular/core';

import { ElementSize } from '@theoliverlear/angular-suite';

import { LoadingWheelColorScheme } from './models/LoadingWheelColorScheme';

@Component({
    selector: 'loading-wheel',
    templateUrl: './loading-wheel.component.html',
    styleUrls: ['./loading-wheel.component.scss'],
    standalone: false,
})
export class LoadingWheelComponent {
    @Input() public visible: boolean = true;
    @Input() public colorScheme: LoadingWheelColorScheme = LoadingWheelColorScheme.Ocean;
    @Input() public size: ElementSize = ElementSize.MEDIUM;

    @HostBinding('style.visibility')
    get visibility(): "visible" | "hidden" {
        return this.visible ? 'visible' : 'hidden';
    }

    @HostBinding('class')
    get hostClasses(): string {
        return `${this.colorScheme} ${this.size}`;
    }

    constructor() {}

    setVisible(visible: boolean): void {
        this.visible = visible;
    }
}
