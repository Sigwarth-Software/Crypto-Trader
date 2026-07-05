import { type Route, type Routes } from '@angular/router'

import { AccountComponent } from '@components/pages/account/account.component'
import { AuthorizeComponent } from '@components/pages/authorize/authorize.component'
import { ChatComponent } from '@components/pages/chat/chat.component'
import { ConsoleComponent } from '@components/pages/console/console.component'
import { CurrenciesComponent } from '@components/pages/currencies/currencies.component'
import { DashboardComponent } from '@components/pages/dashboard/dashboard.component'
import { HomeComponent } from '@components/pages/home/home.component'
import { ModulesComponent } from '@components/pages/modules/modules.component'
import { PortfolioComponent } from '@components/pages/portfolio/portfolio.component'
import { StatisticsComponent } from '@components/pages/statistics/statistics.component'
import { TermsOfServiceComponent } from '@components/pages/terms-of-service/terms-of-service.component'
import { TradeComponent } from '@components/pages/trade/trade.component'
import { TraderComponent } from '@components/pages/trader/trader.component'
import { environment } from '@environments/environment'
import { AccountGuard } from '@guards/account.guard'
import { AuthGuard } from '@guards/auth.guard'
import { DashboardGuard } from '@guards/dashboard.guard'
import {
    SimulatorComponent
} from '@components/pages/simulator/simulator.component'

const isDevelopment: boolean = environment.environmentName === 'development'

export const accountRoute: Route = {
    path: 'account',
    component: AccountComponent,
    // canActivate: isDevelopment ? [] : [AuthGuard],
    canActivate: [AuthGuard],
    data: {
        meta: {
            title: 'Account | Crypto Trader',
        },
    },
}
export const authorizeRoute: Route = {
    path: 'authorize',
    component: AuthorizeComponent,
    canActivate: isDevelopment ? [] : [AccountGuard],
    data: {
        meta: {
            title: 'Authorize | Crypto Trader',
        },
    },
}
export const homeRoute: Route = {
    path: '',
    component: HomeComponent,
    pathMatch: 'full',
    // canActivate: isDevelopment ? [] : [DashboardGuard],
    canActivate: [DashboardGuard],
    data: {
        meta: {
            title: 'Crypto Trader',
            // showNavBar: false,
        },
    },
}
export const modulesRoute: Route = {
    path: 'modules',
    component: ModulesComponent,
    data: {
        meta: {
            title: 'Modules | Crypto Trader',
        },
    },
}
export const portfolioRoute: Route = {
    path: 'portfolio',
    component: PortfolioComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Portfolio | Crypto Trader',
        },
    },
}
export const termsOfServiceRoute: Route = {
    path: 'terms',
    component: TermsOfServiceComponent,
    data: {
        meta: {
            title: 'Terms of Service | Crypto Trader',
        },
    },
}
export const traderRoute: Route = {
    path: 'trader',
    component: TraderComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Trader | Crypto Trader',
        },
    },
}

export const currenciesRoute: Route = {
    path: 'currencies',
    component: CurrenciesComponent,
    data: {
        meta: {
            title: 'Currencies | Crypto Trader',
        },
    },
}

export const chatRoute: Route = {
    path: 'chat',
    component: ChatComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Chat | Crypto Trader',
        },
    },
}
export const consoleRoute: Route = {
    path: 'console',
    component: ConsoleComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Console | Crypto Trader',
        },
    },
}
export const tradeRoute: Route = {
    path: 'trade',
    component: TradeComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Trade | Crypto Trader',
        },
    },
}

export const dashboardRoute: Route = {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Dashboard | Crypto Trader',
        },
    },
}

export const statisticsRoute: Route = {
    path: 'statistics',
    component: StatisticsComponent,
    canActivate: isDevelopment ? [] : [AuthGuard],
    data: {
        meta: {
            title: 'Statistics | Crypto Trader',
        },
    },
}

export const simulatorRoute: Route = {
    path: 'simulator',
    component: SimulatorComponent,
    data: {
        meta: {
            title: 'Simulator | Crypto Trader',
        },
    },
}

export const routes: Routes = [
    accountRoute,
    authorizeRoute,
    homeRoute,
    modulesRoute,
    portfolioRoute,
    termsOfServiceRoute,
    currenciesRoute,
    chatRoute,
    consoleRoute,
    traderRoute,
    tradeRoute,
    dashboardRoute,
    statisticsRoute,
    simulatorRoute,
]
