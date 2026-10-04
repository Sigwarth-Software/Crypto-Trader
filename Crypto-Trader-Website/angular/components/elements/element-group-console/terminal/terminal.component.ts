// terminal.component.ts
import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core'

import { AuthService } from '@http/auth/auth.service'
import { LoggedInService } from '@http/auth/status/logged-in.service'
import { ConsoleCommandService } from '@http/console/console-command.service'
import { TokenStorageService } from '@auth/token-storage.service'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { CryptoTraderConsole } from '@models/console/CryptoTraderConsole'
import { LoggerContext } from '@models/logging/LoggerContext'

/**
 * A terminal component that displays the console.
 */
@Component({
    selector: 'terminal',
    templateUrl: './terminal.component.html',
    styleUrls: ['./terminal.component.scss'],
    standalone: false,
})
export class TerminalComponent implements AfterViewInit, OnDestroy {
    @ViewChild('host', { static: true })
    protected host!: ElementRef<HTMLDivElement>
    private terminal: CryptoTraderConsole | undefined = undefined
    private resizeHandler?: () => void

    constructor(
        private readonly consoleCommandService: ConsoleCommandService,
        private readonly authService: AuthService,
        private readonly loggedInService: LoggedInService,
        private readonly tokenStorageService: TokenStorageService,
        private readonly logger: CryptoTraderLoggerService,
    ) {}

    /** After the view is initialized, create the terminal and add a resize
     *  handler.
     */
    public ngAfterViewInit(): void {
        this.logger.setContext(LoggerContext.Console)
        this.terminal = new CryptoTraderConsole(
            this.host,
            this.consoleCommandService,
            this.authService,
            this.loggedInService,
            this.tokenStorageService,
        )
        this.resizeHandler = this.onResize.bind(this) as () => void
        window.addEventListener('resize', this.resizeHandler, {
            passive: true,
        })
        requestAnimationFrame((): void => this.terminal?.fit())
        setTimeout((): void => this.terminal?.fit(), 0)
    }

    private onResize(): void {
        return this.terminal?.fit()
    }

    /**
     * On destruction, dispose the terminal and remove the resize handler.
     */
    public ngOnDestroy(): void {
        this.logger.info('Terminal component destroyed')
        if (this.resizeHandler) {
            window.removeEventListener('resize', this.resizeHandler)
            this.resizeHandler = undefined
        }
        if (this.terminal) {
            this.terminal.dispose()
        }
    }
}
