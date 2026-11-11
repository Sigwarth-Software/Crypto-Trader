import { CommonModule } from '@angular/common'
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { AngularSuiteModule } from '@theoliverlear/angular-suite'

import { UniversalModule } from '../shared/universal.module'
import { ChatBubbleComponent } from 'angular/components/elements/element-group-chat/chat-bubble/chat-bubble.component'
import { ChatInputComponent } from 'angular/components/elements/element-group-chat/chat-input/chat-input.component'
import { ChatOverlayComponent } from 'angular/components/elements/element-group-chat/chat-overlay/chat-overlay.component'
import { ChatPanelComponent } from 'angular/components/elements/element-group-chat/chat-panel/chat-panel.component'
import { ChatComponent } from 'angular/components/pages/chat/chat.component'

const chatComponents = [
    ChatBubbleComponent,
    ChatInputComponent,
    ChatOverlayComponent,
    ChatPanelComponent,
    ChatComponent,
]

/**
 * The module for Crypto-Trader-Chat's feature in Crypto-Trader-Website.
 */
@NgModule({
    declarations: [...chatComponents],
    imports: [CommonModule, FormsModule, AngularSuiteModule, UniversalModule],
    exports: [...chatComponents],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class ChatModule {}
