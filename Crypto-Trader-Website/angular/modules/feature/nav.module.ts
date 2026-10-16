import { CommonModule } from '@angular/common'
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'
import { RouterModule } from '@angular/router'
import { AngularSuiteModule } from '@theoliverlear/angular-suite'

import { UniversalModule } from '../shared/universal.module'
import { ExitAnchorComponent } from 'angular/components/elements/element-group-nav/exit-anchor/exit-anchor.component'
import { HomeAnchorComponent } from 'angular/components/elements/element-group-nav/home-anchor/home-anchor.component'
import { NavArrowsComponent } from 'angular/components/elements/element-group-nav/nav-arrows/nav-arrows.component'
import { NavBarItemComponent } from 'angular/components/elements/element-group-nav/nav-bar-item/nav-bar-item.component'
import { NavBarProfilePictureComponent } from 'angular/components/elements/element-group-nav/nav-bar-profile-picture/nav-bar-profile-picture.component'
import { NavBarComponent } from 'angular/components/elements/element-group-nav/nav-bar/nav-bar.component'
import { NavBumperComponent } from 'angular/components/elements/element-group-nav/nav-bumper/nav-bumper.component'
import { NavConsoleComponent } from 'angular/components/elements/element-group-nav/nav-console/nav-console.component'
import { NavUpgradeComponent } from 'angular/components/elements/element-group-nav/nav-upgrade/nav-upgrade.component'
import { NavChatComponent } from '@components/elements/element-group-nav/nav-chat/nav-chat.component'

const navComponents = [
    ExitAnchorComponent,
    HomeAnchorComponent,
    NavArrowsComponent,
    NavBarComponent,
    NavBarItemComponent,
    NavBarProfilePictureComponent,
    NavBumperComponent,
    NavChatComponent,
    NavConsoleComponent,
    NavUpgradeComponent,
]

/**
 * The module for the navigation components.
 */
@NgModule({
    declarations: [...navComponents],
    imports: [CommonModule, RouterModule, AngularSuiteModule, UniversalModule],
    exports: [...navComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class NavModule {}
