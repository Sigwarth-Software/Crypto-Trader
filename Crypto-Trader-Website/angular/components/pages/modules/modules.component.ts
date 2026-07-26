import { Component, OnInit } from '@angular/core'

import { TagType } from '@theoliverlear/angular-suite'
import { StatItem } from '@components/elements/element-group-system/stat-strip/stat-strip.component'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { modulesPageTitleStripe } from '@assets/page-title-stripe.assets'
import { LoggerContext } from '@models/logging/LoggerContext'
import { PageTitleStripe } from '@components/elements/element-group-system/page-title-stripe/models/PageTitleStripe'

/**
 * The promotional page for the modules of Crypto Trader.
 */
@Component({
    selector: 'modules',
    standalone: false,
    templateUrl: './modules.component.html',
    styleUrls: ['./modules.component.scss'],
})
export class ModulesComponent implements OnInit {
    constructor(private readonly logger: CryptoTraderLoggerService) {}

    /**
     * On init, set the logger context.
     */
    public ngOnInit(): void {
        this.logger.setContext(LoggerContext.Promo)
    }

    // TODO: Move to assets file.
    protected readonly statItems: StatItem[] = [
        { icon: '🧩', label: '20 Modules', value: 20, suffix: ' Modules' },
        { icon: '⚙️', label: '5 Languages', value: 5, suffix: ' Languages' },
        { icon: '🔒', label: 'Open Source' },
        { icon: '🚀', label: '24/7 Operation' },
        { icon: '📊', label: '30+ Subprojects', value: 30, suffix: '+ Subprojects' },
    ]

    protected readonly TagType: typeof TagType = TagType
    protected readonly modulesPageTitleStripe: PageTitleStripe = modulesPageTitleStripe
}
