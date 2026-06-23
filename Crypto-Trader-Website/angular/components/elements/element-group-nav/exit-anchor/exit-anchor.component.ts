// exit-anchor.component.ts
import { Component, HostListener } from '@angular/core';
import { Router } from '@angular/router';

import { ElementLink } from '@theoliverlear/angular-suite';
import { homeElementLink } from '@assets/element-link.assets';
import { exitIcon, ImageAsset } from '@assets/image.assets';
import { LogoutService } from '@http/auth/access/logout.service';
import { TokenStorageService } from '@auth/token-storage.service';
import { AuthResponse } from '@models/auth/types';

/** An anchor that logs the user out and navigates to the auth page.
 *
 */
@Component({
    selector: 'exit-anchor',
    templateUrl: './exit-anchor.component.html',
    styleUrls: ['./exit-anchor.component.scss'],
    standalone: false,
})
export class ExitAnchorComponent {
    constructor(
        private readonly logoutService: LogoutService,
        private readonly tokenStorageService: TokenStorageService,
        private readonly router: Router,
    ) {}

    /** Clears the token and navigates to the authorize page on click.
     *
     */
    @HostListener('click')
    public onClick(): void {
        // Important: Do NOT clear the token before making the logout request,
        // otherwise the Authorization header won't be attached and the server
        // cannot blacklist the token. Clear it after the request completes.
        this.logoutService.logout().subscribe({
            next: (authResponse: AuthResponse): void => {
                console.log('Logged out: ', authResponse);
            },
            error: (error): void => {
                // Even if logout fails, clear local token to avoid lingering client auth
                console.warn('Logout request error (ignored):', error);
            },
            complete: (): void => {
                try {
                    this.tokenStorageService.clear();
                } catch {
                    console.error('Failed to clear token storage');
                }
                void this.router.navigate(['/authorize']).then((): void => {
                    console.log('Logout complete');
                });
            },
        });
    }

    protected readonly exitIcon: ImageAsset = exitIcon;
    protected readonly homeElementLink: ElementLink = homeElementLink;
}
