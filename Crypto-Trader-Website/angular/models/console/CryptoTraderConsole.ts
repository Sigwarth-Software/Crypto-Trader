import { type ElementRef } from '@angular/core'
import { FitAddon } from '@xterm/addon-fit'
import { WebLinksAddon } from '@xterm/addon-web-links'
import { type Terminal } from '@xterm/xterm'
import {
    catchError,
    concatMap,
    interval,
    type Subscription,
    type Observable,
    of,
    Subject,
    tap,
} from 'rxjs'

import { AuthType, HashPasswordService } from '@theoliverlear/angular-suite'
import { createTerminal } from '@assets/console.assets'
import { type AuthService } from '@http/auth/auth.service'
import { type LoggedInService } from '@http/auth/status/logged-in.service'
import { type ConsoleCommandService } from '@http/console/console-command.service'
import { type TokenStorageService } from '@auth/token-storage.service'

import { type AuthResponse, type LoginRequest, type SignupRequest } from '../auth/types'
import { ConsoleMode } from './ConsoleMode'
import { LocalConsoleCommand } from './LocalConsoleCommand'

/** The model for Crypto Trader Console functionality.
 *
 */
export class CryptoTraderConsole {
    private readonly _terminal: Terminal
    private readonly _fit: FitAddon
    private readonly _links: WebLinksAddon
    private _buffer: string = ''
    private readonly _elementRef: ElementRef<HTMLDivElement>
    private readonly _consoleCommandService: ConsoleCommandService
    private readonly _authService: AuthService
    private readonly _tokenStorageService: TokenStorageService
    private readonly _command$: Subject<string>
    private _spinnerSubscription: Subscription | null
    private static readonly PROMPT_CHAR: string = '$ '
    private readonly _history: string[]
    private _historyIndex: number
    private _authType: AuthType | null

    private static readonly RED = '\x1b[31m' as const
    private static readonly GREEN = '\x1b[32m' as const
    private static readonly YELLOW = '\x1b[33m' as const
    private static readonly CYAN = '\x1b[36m' as const
    private static readonly RESET = '\x1b[0m' as const
    private static readonly BOLD = '\x1b[1m' as const

    private _authEmail: string
    private _authPassword: string | null
    private readonly _loggedInService: LoggedInService
    private _consoleMode: ConsoleMode

    constructor(
        elementRef: ElementRef<HTMLDivElement>,
        consoleCommandService: ConsoleCommandService,
        authService: AuthService,
        loggedInService: LoggedInService,
        tokenStorageService: TokenStorageService,
    ) {
        this._elementRef = elementRef
        this._terminal = createTerminal()
        this._fit = new FitAddon()
        this._links = new WebLinksAddon()
        this._consoleCommandService = consoleCommandService
        this._loggedInService = loggedInService
        this._authService = authService
        this._tokenStorageService = tokenStorageService
        this._command$ = new Subject<string>()
        this._spinnerSubscription = null
        this._history = []
        this._historyIndex = -1
        this._authEmail = ''
        this._authPassword = null
        this._consoleMode = ConsoleMode.NORMAL
        this._authType = null
        this.initTerminal()
        this.initReactivePipeline()
    }

    private initTerminal(): void {
        this._terminal.loadAddon(this._fit)
        this._terminal.loadAddon(this._links)
        this._terminal.open(this._elementRef.nativeElement)
        this._fit.fit()
        // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
        this._terminal.onData(this.onData.bind(this))
        // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
        this._terminal.onKey(this.onKey.bind(this))
        this.writeDefaultText()
        this.showPrompt()
    }

    private writeDefaultText(): void {
        let welcomeText: string = CryptoTraderConsole.getBoldText(
            'Welcome to the Crypto Trader console!',
        )
        welcomeText = CryptoTraderConsole.getColoredText(welcomeText, CryptoTraderConsole.CYAN)
        this._terminal.writeln(welcomeText)
        const helpText: string = 'Type "help" to see commands.'
        this._terminal.writeln(helpText)
        this._terminal.writeln('')
    }

    private initReactivePipeline(): void {
        this._command$
            .pipe(
                // TODO: Replace object literal with type.
                concatMap((line: string): Observable<{ consoleOutput: string }> => {
                    if (!line) {
                        if (this.isLocalCommand(line)) {
                            this.execute(line)
                            return of({ consoleOutput: '' })
                        }
                    }
                    if (this.isLocalCommand(line)) {
                        this.execute(line)
                        return of({ consoleOutput: '' })
                    }
                    return this.runWithSpinner(
                        'Executing command...',
                        this._consoleCommandService.executeCommand({ commandText: line }).pipe(
                            catchError((err): Observable<{ consoleOutput: string }> => {
                                this._terminal.writeln(
                                    `${CryptoTraderConsole.RED}Error executing command: ${err?.message ?? err}${CryptoTraderConsole.RESET}`,
                                )
                                return of({ consoleOutput: '' })
                            }),
                        ),
                    )
                }),
                tap((response): void => {
                    if (response.consoleOutput) {
                        this._terminal.writeln(response.consoleOutput)
                    }
                    if (this._consoleMode === ConsoleMode.NORMAL) {
                        this.showPrompt()
                    }
                }),
            )
            .subscribe()
    }

    private onData(data: string): void {
        const code: number = data.charCodeAt(0)

        if (data === '\r') {
            // Enter
            this._terminal.write('\r\n')
            switch (this._consoleMode) {
                case ConsoleMode.NORMAL:
                    this.handleNormalEnter()
                    break
                case ConsoleMode.EMAIL:
                    this.handleAuthEmailEnter()
                    break
                case ConsoleMode.PASSWORD:
                    this.handleAuthPasswordEnter()
                    break
                case ConsoleMode.CONFIRM_PASSWORD:
                    this.handleAuthConfirmPasswordEnter()
                    break
            }
            return
        }
        if (code === 127 || data === '\x7f') {
            // Backspace
            if (this._buffer.length) {
                this._buffer = this._buffer.slice(0, -1)
                this._terminal.write('\b \b')
            }
            return
        }
        if (code >= 32 && code <= 126) {
            // Printable
            this._buffer += data

            if (this.shouldHideInput()) {
                this._terminal.write('*')
            } else {
                this._terminal.write(data)
            }
        }
    }

    private shouldHideInput(): boolean {
        return (
            this._consoleMode === ConsoleMode.PASSWORD ||
            this._consoleMode === ConsoleMode.CONFIRM_PASSWORD
        )
    }

    /** Handles the logic of confirming password during sign-up flow.
     *
     */
    handleAuthConfirmPasswordEnter(): void {
        const confirmPassword: string = this._buffer
        this._buffer = ''

        const email: string | null = this._authEmail

        if (!email || this._authPassword == null) {
            this._terminal.writeln(
                `${CryptoTraderConsole.RED}Internal error: missing state in auth flow.${CryptoTraderConsole.RESET}`,
            )
            this._consoleMode = ConsoleMode.NORMAL
            this.showPrompt()
            return
        }

        if (confirmPassword !== this._authPassword) {
            this._terminal.writeln(
                `${CryptoTraderConsole.RED}Passwords do not match. Please start again with "auth --signup".${CryptoTraderConsole.RESET}`,
            )
            this._authPassword = null
            this._consoleMode = ConsoleMode.NORMAL
            this.showPrompt()
            return
        }
        this._terminal.writeln('Creating account...')
        this._consoleMode = ConsoleMode.NORMAL

        if (this._authType === AuthType.SIGN_UP) {
            this.authenticate(email, this._authPassword)
        }
        this._authPassword = null
    }

    private onKey(event: { key: string; domEvent: KeyboardEvent }): void {
        const { key, domEvent } = event

        if (domEvent.key === 'ArrowUp') {
            domEvent.preventDefault()
            this.showPreviousHistory()
            return
        }

        if (domEvent.key === 'ArrowDown') {
            domEvent.preventDefault()
            this.showNextHistory()
            return
        }

        if (domEvent.key === 'Backspace' && domEvent.ctrlKey) {
            domEvent.preventDefault()
            this.deletePreviousWord()
            return
        }
    }

    private deletePreviousWord(): void {
        if (!this._buffer.length) {
            return
        }

        let i: number = this._buffer.length - 1
        while (i >= 0 && this._buffer[i] === ' ') {
            i--
        }

        while (i >= 0 && this._buffer[i] !== ' ') {
            i--
        }

        this._buffer = this._buffer.slice(0, i + 1)
        this.clearCurrentLine()
        this.redrawPromptAndBuffer()
    }

    private clearCurrentLine(): void {
        this._terminal.write('\x1b[2K\r')
    }

    private redrawPromptAndBuffer(): void {
        this._terminal.write(CryptoTraderConsole.PROMPT_CHAR + this._buffer)
    }

    private showPreviousHistory(): void {
        if (this._history.length === 0) {
            return
        }
        if (this._historyIndex > 0) {
            this._historyIndex--
        } else {
            this._historyIndex = 0
        }
        this._buffer = this._history[this._historyIndex] ?? ''
        this.clearCurrentLine()
        this.redrawPromptAndBuffer()
    }

    private showNextHistory(): void {
        if (this._history.length === 0) {
            return
        }
        if (this._historyIndex < this._history.length - 1) {
            this._historyIndex++
            this._buffer = this._history[this._historyIndex] ?? ''
        } else {
            this._historyIndex = this._history.length
            this._buffer = ''
        }
        this.clearCurrentLine()
        this.redrawPromptAndBuffer()
    }

    /** Utility for removing excess whitespace for input validation.
     *
     */
    removeTrailingSpaces(): void {
        this._buffer = this._buffer.trim()
    }

    private showPrompt(): void {
        this._terminal.write(CryptoTraderConsole.PROMPT_CHAR)
    }

    private startSpinner(message: string): void {
        this.stopSpinner()

        const frames: string[] = ['|', '/', '-', '\\']
        let frameIndex: number = 0

        const CURSOR_RESET_BASE: string = `\r\x1b[2K`
        this._terminal.write(`${CURSOR_RESET_BASE}${frames[frameIndex]} ${message}`)
        this._spinnerSubscription = interval(80).subscribe((): void => {
            const frame: string = frames[frameIndex % frames.length] ?? '|'
            this._terminal.write(`\r\x1b[2K${frame} ${message}`)
            frameIndex++
        })
    }

    private stopSpinner(): void {
        this._spinnerSubscription?.unsubscribe()
        this._spinnerSubscription = null
        this._terminal.write('\r\x1b[2K')
    }

    private runWithSpinner<T>(message: string, source$: Observable<T>): Observable<T> {
        this.startSpinner(message)
        return source$.pipe(
            tap({
                next: (): void => this.stopSpinner(),
                error: (): void => this.stopSpinner(),
            }),
        )
    }

    private execute(input: string): void {
        const [command, ...args] = input.split(/\s+/)
        switch (command) {
            case '':
                break
            case 'auth':
                this.handleAuthFlow(args)
                break
            case 'time':
                this._terminal.writeln(new Date().toString())
                break
            case 'clear':
                this._terminal.clear()
                this.writeDefaultText()
                break
            default:
                this.handleUnknownCommand(command)
        }
    }

    /** Executes the authorization via CLI.
     *
     * @param args
     */
    handleAuthFlow(args: string[]): void {
        const containsLoginArg: boolean = args.includes('--login')
        const containsSignupArg: boolean = args.includes('--signup')
        const containsLogoutArg: boolean = args.includes('--logout')
        if (containsLoginArg) {
            this._authType = AuthType.LOGIN
        } else if (containsSignupArg) {
            this._authType = AuthType.SIGN_UP
        } else if (containsLogoutArg) {
            this.logout()
            this._terminal.writeln(
                `${CryptoTraderConsole.GREEN}Logged out successfully.${CryptoTraderConsole.RESET}`,
            )
            return
        } else {
            this._terminal.writeln(
                `${CryptoTraderConsole.RED}Invalid auth command.${CryptoTraderConsole.RESET}`,
            )
            return
        }
        this.startAuthFlow()
    }

    private handleUnknownCommand(command: string): void {
        this._terminal.writeln(
            `${CryptoTraderConsole.RED}Unknown command: ${command}${CryptoTraderConsole.RESET}`,
        )
    }

    private startAuthFlow(): void {
        this._consoleMode = ConsoleMode.EMAIL
        this._buffer = ''
        this._terminal.writeln('Starting authentication...')
        this._terminal.write('Email: ')
    }

    private handleNormalEnter(): void {
        const line: string = this._buffer.trim()

        if (line.length > 0) {
            if (this._history.length === 0 || this._history[this._history.length - 1] !== line) {
                this._history.push(line)
            }
            this._historyIndex = this._history.length
        }

        this._command$.next(line)
        this._buffer = ''
    }

    private handleAuthEmailEnter(): void {
        const email: string = this._buffer.trim()
        this._buffer = ''

        if (!email) {
            this._terminal.writeln('Email cannot be empty. Try again or type Ctrl+C to cancel.')
            this._terminal.write('Email: ')
            return
        }

        this._authEmail = email
        this._consoleMode = ConsoleMode.PASSWORD

        this._terminal.write('Password: ')
    }

    private handleAuthPasswordEnter(): void {
        const password: string = this._buffer
        this._buffer = ''

        const email: string | null = this._authEmail

        if (!email) {
            this._terminal.writeln(
                `${CryptoTraderConsole.RED}Internal error: missing email in auth flow.${CryptoTraderConsole.RESET}`,
            )
            this._consoleMode = ConsoleMode.NORMAL
            this.showPrompt()
            return
        }

        if (this._authType === AuthType.SIGN_UP) {
            this._authPassword = password
            this._consoleMode = ConsoleMode.CONFIRM_PASSWORD
            this._terminal.write('Confirm password: ')
            return
        }
        if (this._authType === AuthType.LOGIN) {
            this._terminal.writeln('Authenticating...')
            this.authenticate(email, password)
        }
    }

    private authenticate(email: string, rawPassword: string): void {
        const hasher: HashPasswordService = new HashPasswordService()
        const hashedPassword: string = hasher.hashPassword(rawPassword)

        if (this._authType === AuthType.LOGIN) {
            this.login(email, hashedPassword)
        } else if (this._authType === AuthType.SIGN_UP) {
            this.signup(email, hashedPassword)
        } else {
            this._terminal.writeln(
                `${CryptoTraderConsole.RED}Internal error: unknown auth type.${CryptoTraderConsole.RESET}`,
            )
            this._consoleMode = ConsoleMode.NORMAL
            this.showPrompt()
        }
    }

    private logout(): void {
        this._authService
            .logout()
            .pipe(
                tap((response: AuthResponse): void => {
                    if (!response?.authorized) {
                        this._terminal.writeln(
                            `${CryptoTraderConsole.GREEN}Logged out successfully.${CryptoTraderConsole.RESET}`,
                        )
                        this._tokenStorageService.clear()
                    }
                    this._loggedInService.isLoggedIn().subscribe()
                }),
                catchError((err): Observable<null> => {
                    const message = err?.error?.message ?? err?.message ?? 'Unknown error'
                    this._terminal.writeln(
                        `${CryptoTraderConsole.RED}Logout failed: ${message}${CryptoTraderConsole.RESET}`,
                    )
                    return of(null)
                }),
            )
            .subscribe({
                complete: (): void => this.showPrompt(),
            })
    }

    private signup(email: string, hashedPassword: string): void {
        const signupRequest: SignupRequest = {
            email: email,
            password: hashedPassword,
        }

        this._authService
            .signup(signupRequest)
            .pipe(
                tap((response): void => {
                    if (response?.authorized) {
                        this._tokenStorageService.setToken(response.token ?? null)
                        this._terminal.writeln(
                            `${CryptoTraderConsole.GREEN}Authentication successful.${CryptoTraderConsole.RESET}`,
                        )
                        this._consoleMode = ConsoleMode.NORMAL
                        this._loggedInService.isLoggedIn().subscribe()
                    } else {
                        this._terminal.writeln(
                            `${CryptoTraderConsole.YELLOW}Authentication response received but not authorized.${CryptoTraderConsole.RESET}`,
                        )
                        this._consoleMode = ConsoleMode.NORMAL
                    }
                }),
                catchError((err): Observable<null> => {
                    const message = err?.error?.message ?? err?.message ?? 'Unknown error'
                    this._terminal.writeln(
                        `${CryptoTraderConsole.RED}Authentication failed: ${message}${CryptoTraderConsole.RESET}`,
                    )
                    this._consoleMode = ConsoleMode.NORMAL
                    return of(null)
                }),
            )
            .subscribe({
                complete: (): void => {
                    this.showPrompt()
                },
            })
    }

    private login(email: string, hashedPassword: string): void {
        const loginRequest: LoginRequest = {
            email: email,
            password: hashedPassword,
        }

        this._authService
            .login(loginRequest)
            .pipe(
                tap((response): void => {
                    if (response?.authorized) {
                        this._tokenStorageService.setToken(response.token ?? null)
                        this._terminal.writeln(
                            `${CryptoTraderConsole.GREEN}Authentication successful.${CryptoTraderConsole.RESET}`,
                        )
                        this._consoleMode = ConsoleMode.NORMAL
                        this._loggedInService.isLoggedIn().subscribe()
                    } else {
                        this._terminal.writeln(
                            `${CryptoTraderConsole.YELLOW}Authentication response received but not authorized.${CryptoTraderConsole.RESET}`,
                        )
                        this._consoleMode = ConsoleMode.NORMAL
                    }
                }),
                catchError((err): Observable<null> => {
                    const message = err?.error?.message ?? err?.message ?? 'Unknown error'
                    this._terminal.writeln(
                        `${CryptoTraderConsole.RED}Authentication failed: ${message}${CryptoTraderConsole.RESET}`,
                    )
                    this._consoleMode = ConsoleMode.NORMAL
                    return of(null)
                }),
            )
            .subscribe({
                complete: (): void => {
                    this.showPrompt()
                },
            })
    }

    /** Wrapper for fitting terminal to proper dimensions.
     *
     */
    public fit(): void {
        this._fit.fit()
    }

    /** Detects whether commands should be executed on client or server.
     *
     * @param command The command to execute.
     */
    public isLocalCommand(command: string): boolean {
        const [firstCommand, ...args] = command.split(/\s+/)
        return LocalConsoleCommand.isLocalCommand(firstCommand)
    }

    /** Wrapper for destroying the terminal.
     *
     */
    public dispose(): void {
        this._terminal.dispose()
    }

    private static getColoredText(text: string, color: string): string {
        return `${color}${text}${CryptoTraderConsole.RESET}`
    }

    private static getBoldText(text: string): string {
        return `${CryptoTraderConsole.BOLD}${text}${CryptoTraderConsole.RESET}`
    }
}
