// flip-words.component.ts
import { Component, Input, OnDestroy, OnInit } from '@angular/core'
import { TagType } from '@theoliverlear/angular-suite'

/**
 * A component that flips through options of phrases.
 */
@Component({
    selector: 'flip-words',
    templateUrl: './flip-words.component.html',
    styleUrls: ['./flip-words.component.scss'],
    standalone: false,
})
export class FlipWordsComponent implements OnInit, OnDestroy {
    @Input() public words: string[] = []
    @Input() public periodMs: number = 2000
    @Input() public tagType: TagType = TagType.SPAN
    @Input() public shouldPunctuate: boolean = false

    public current: number = 0
    public swap: boolean = false
    private flipOutTimeout?: ReturnType<typeof setTimeout>
    private nextWordTimeout?: ReturnType<typeof setTimeout>

    /**
     * On initialization, schedule the text flipping.
     */
    public ngOnInit(): void {
        this.schedule()
    }

    /**
     * On destruction, clear timeouts.
     */
    public ngOnDestroy(): void {
        clearTimeout(this.flipOutTimeout)
        clearTimeout(this.nextWordTimeout)
    }

    // TODO: Move to utils or service file.
    private schedule(): void {
        const ANIMATION_DELAY_MS: number = 350
        this.flipOutTimeout = setTimeout((): void => {
            this.swap = true
        }, this.periodMs - ANIMATION_DELAY_MS)
        this.nextWordTimeout = setTimeout((): void => {
            this.current = (this.current + 1) % this.words.length
            this.swap = false
            this.schedule()
        }, this.periodMs)
    }

    protected getCurrentPhrase(): string {
        return `${this.words[this.current]}${this.shouldPunctuate ? '.' : ''}`
    }
}
