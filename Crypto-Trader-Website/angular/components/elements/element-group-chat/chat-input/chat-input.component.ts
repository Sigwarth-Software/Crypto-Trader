// chat-input.component.ts
import { Component, EventEmitter, Input, Output } from '@angular/core'

/**
 * An input component for Crypto-Trader-Chat.
 */
@Component({
    selector: 'chat-input',
    templateUrl: './chat-input.component.html',
    styleUrls: ['./chat-input.component.scss'],
    standalone: false,
})
export class ChatInputComponent {
    @Input() public value: string = ''
    @Output() public valueChange: EventEmitter<string> = new EventEmitter<string>()
    @Output() public send: EventEmitter<void> = new EventEmitter<void>()
    @Output() public keyDown: EventEmitter<KeyboardEvent> = new EventEmitter<KeyboardEvent>()

    /**
     * On input, set the value and emit the event.
     * @param event
     */
    public onInput(event: Event): void {
        const target: HTMLTextAreaElement = event.target as HTMLTextAreaElement
        this.value = target.value
        this.valueChange.emit(this.value)
    }

    /**
     * On key down, emit the event.
     * @param event
     */
    public onKeyDown(event: KeyboardEvent): void {
        this.keyDown.emit(event)
    }

    /**
     * On sending input, emit the event.
     */
    public onSend(): void {
        this.send.emit()
    }
}
