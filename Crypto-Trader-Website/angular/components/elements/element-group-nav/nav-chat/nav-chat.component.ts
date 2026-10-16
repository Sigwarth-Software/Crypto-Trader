// nav-chat.component.ts
import { Component } from '@angular/core';
import {
    BaseLoggingElementComponent
} from '@components/elements/element-group-system/base-logging-element/base-logging-element.component'
import { LoggerContext } from '@models/logging/LoggerContext'
import { chatElementLink } from '@assets/element-link.assets'
import { whiteChatModuleIcon } from '@assets/image.assets'

@Component({
    selector: 'nav-chat',
    templateUrl: './nav-chat.component.html',
    styleUrls: ['./nav-chat.component.scss'],
    standalone: false,
})
export class NavChatComponent extends BaseLoggingElementComponent {
    constructor() {
        super()
        this.loggerContext = LoggerContext.Navigation
    }

    protected readonly chatElementLink = chatElementLink
    protected readonly whiteChatModuleIcon = whiteChatModuleIcon
}
