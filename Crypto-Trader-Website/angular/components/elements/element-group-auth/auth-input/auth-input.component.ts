// auth-input.component.ts
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core'

import { InputType, TagType } from '@theoliverlear/angular-suite'

import { AuthInputType } from './models/AuthInputType'

/** A text input for authentication.
 *
 */
@Component({
    selector: 'auth-input',
    standalone: false,
    templateUrl: './auth-input.component.html',
    styleUrls: ['./auth-input.component.scss'],
})
export class AuthInputComponent implements OnInit {
    @Input() public authInputType: AuthInputType
    @Input() public authTypeString: string = ''
    @Output() public inputChange: EventEmitter<string> = new EventEmitter<string>()
    protected inputText: string = ''
    constructor() {}
    /** Emits the input text to the parent component.
     *
     */
    protected emitInputText(): void {
        this.inputChange.emit(this.inputText)
    }
    /**
     * Updates the input text and emits it.
     * @param text The new input text.
     */
    public updateInputText(text: string): void {
        this.inputText = text
        this.emitInputText()
    }
    /**
     * On init, initialize the auth text string.
     */
    public ngOnInit(): void {
        this.initAuthText()
    }

    private initAuthText(): void {
        if (this.authInputType !== AuthInputType.AgreeTerms) {
            this.authTypeString = `${this.authInputType}:`
        } else {
            this.authTypeString = this.authInputType
        }
        this.authTypeString = this.authTypeString.toUpperCase()
    }

    /**
     * Returns a clean label for the input field.
     * @returns The label string.
     */
    public getLabel(): string {
        return this.authInputType
    }

    /**
     * Returns the placeholder text for the input field.
     * @returns The placeholder string.
     */
    public getPlaceholder(): string {
        switch (this.authInputType) {
            case AuthInputType.Email:
                return 'you@example.com'
            case AuthInputType.Password:
                return 'Enter your password'
            case AuthInputType.ConfirmPassword:
                return 'Confirm your password'
            case AuthInputType.Username:
                return 'Enter your username'
            default:
                return ''
        }
    }

    /**
     * Returns whether this input is the terms checkbox.
     * @returns True if this is the terms input.
     */
    public isTermsInput(): boolean {
        return this.authInputType === AuthInputType.AgreeTerms
    }

    /**
     * Returns the input type based on the auth input type.
     * @returns The corresponding InputType.
     */
    public getInputType(): InputType {
        switch (this.authInputType) {
            case AuthInputType.Username:
                return InputType.TEXT
            case AuthInputType.Email:
                return InputType.EMAIL
            case AuthInputType.Password:
            case AuthInputType.ConfirmPassword:
                return InputType.PASSWORD
            case AuthInputType.AgreeTerms:
                return InputType.CHECKBOX
            default:
                return InputType.TEXT
        }
    }

    protected readonly TagType: typeof TagType = TagType
}
