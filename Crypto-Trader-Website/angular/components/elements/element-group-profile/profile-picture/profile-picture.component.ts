import { Component, Input, ViewChild } from '@angular/core'

import { SsImgComponent } from '@theoliverlear/angular-suite'
import { ImageAsset, profileIcon } from '@assets/image.assets'

/**
 * A component for displaying a profile picture.
 */
@Component({
    selector: 'profile-picture',
    standalone: false,
    templateUrl: './profile-picture.component.html',
    styleUrls: ['./profile-picture.component.scss'],
})
export class ProfilePictureComponent {
    @Input() public userId: number = 0
    @Input() public isHeadlineProfilePicture: boolean = false
    @Input() public imageAsset: ImageAsset = profileIcon
    @ViewChild('profilePictureImage') public profilePictureImage: SsImgComponent
    constructor() {}
}
