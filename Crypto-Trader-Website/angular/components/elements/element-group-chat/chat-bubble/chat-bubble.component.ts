// chat-bubble.component.ts
import { Component, Input } from '@angular/core'
import hljs from 'highlight.js'
import { marked } from 'marked'

import { ChatMessage } from '@models/chat/types'

marked.use({
    renderer: {
        code(code: string, infostring: string | undefined): string {
            const language: string = infostring?.trim().split(/\s+/)[0] ?? ''
            if (hljs.getLanguage(language)) {
                const highlighted: string = hljs.highlight(code, { language }).value
                return `<pre><code class="hljs language-${language}">${highlighted}</code></pre>\n`
            }
            const autoHighlighted: string = hljs.highlightAuto(code).value
            return `<pre><code class="hljs">${autoHighlighted}</code></pre>\n`
        },
    },
})

/**
 * A component showing Crypto-Trader-Chat bubbles.
 */
@Component({
    selector: 'chat-bubble',
    templateUrl: './chat-bubble.component.html',
    styleUrls: ['./chat-bubble.component.scss'],
    standalone: false,
})
export class ChatBubbleComponent {
    // TODO: Remove the non-null assertion.
    @Input() public message!: ChatMessage

    /**
     * The message content parsed from Markdown into HTML.
     */
    public get renderedContent(): string {
        return marked.parse(this.message.content ?? '') as string
    }
}
