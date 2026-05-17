import {
    Component,
    HostListener,
    Input,
    OnChanges,
    OnInit,
    SimpleChanges,
    ViewChild,
} from '@angular/core'

import { ElementLink, SsAnchorComponent } from '@theoliverlear/angular-suite'
import { ImageAsset } from '@assets/imageAssets'

import { NavBarItemOption } from './models/NavBarItemOption'

/** A nav bar item that can be clicked to navigate to a different page.
 *
 */
@Component({
    selector: 'nav-bar-item',
    standalone: false,
    templateUrl: './nav-bar-item.component.html',
    styleUrls: ['./nav-bar-item.component.scss'],
})
export class NavBarItemComponent implements OnInit, OnChanges {
    protected elementLink: ElementLink
    protected imageAsset: ImageAsset
    @Input() public navBarItemOption: NavBarItemOption
    @ViewChild(SsAnchorComponent) protected anchorComponent: SsAnchorComponent | undefined
    constructor() {}

    /** WIP
     *
     */
    public ngOnInit(): void {
        if (this.elementLink) {
            // this.convertTextToUpper();
        }
    }

    /** On nav bar item changes, load their links and images.
     *
     * @param changes
     */
    public ngOnChanges(changes: SimpleChanges): void {
        if ('navBarItemOption' in changes) {
            this.elementLink = NavBarItemOption.getElementLink(this.navBarItemOption)
            this.imageAsset = NavBarItemOption.getImageAsset(this.navBarItemOption)
        }
    }

    /** On click, click the child anchor component.
     *
     */
    @HostListener('click')
    public onClick(): void {
        this.anchorComponent?.onClick()
    }
}
