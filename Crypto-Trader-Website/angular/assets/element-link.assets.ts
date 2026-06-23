import { ElementLink, TagType, TargetType, TextElementLink } from '@theoliverlear/angular-suite'

export const homeElementLink: ElementLink = new ElementLink('/', TargetType.SELF, false)

export const navBarHomeLink: TextElementLink = new TextElementLink(
    '',
    TargetType.SELF,
    false,
    'Home',
    TagType.H4,
)

export const currenciesElementLink: ElementLink = new ElementLink(
    '/currencies',
    TargetType.SELF,
    false,
)

export const traderElementLink: ElementLink = new ElementLink('/trader', TargetType.SELF, false)

export const portfolioElementLink: ElementLink = new ElementLink(
    '/portfolio',
    TargetType.SELF,
    false,
)

export const tradeElementLink: ElementLink = new ElementLink('/trade', TargetType.SELF, false)

export const statisticsElementLink: ElementLink = new ElementLink(
    '/statistics',
    TargetType.SELF,
    false,
)

export const navBarCurrenciesTextLink: TextElementLink = new TextElementLink(
    '/currencies',
    TargetType.SELF,
    false,
    'Currencies',
    TagType.H4,
)

export const navBarPortfolioTextLink: TextElementLink = new TextElementLink(
    '/portfolio',
    TargetType.SELF,
    false,
    'Portfolio',
    TagType.H4,
)
export const navBarTraderTextLink: TextElementLink = new TextElementLink(
    '/trader',
    TargetType.SELF,
    false,
    'Trader',
    TagType.H4,
)

export const simulatorElementLink: ElementLink = new ElementLink(
    '/simulator',
    TargetType.SELF,
    false,
)

export const navBarAccountLink: ElementLink = new ElementLink('/account', TargetType.SELF, false)

export const getStartedElementLink: TextElementLink = new TextElementLink(
    '/authorize',
    TargetType.SELF,
    false,
    'Get Started',
    TagType.H4,
)

export const repositoryElementLink: TextElementLink = new TextElementLink(
    'https://github.com/Sigwarth-Software/Crypto-Trader',
    TargetType.BLANK,
    false,
    'Explore Code',
    TagType.H4,
)

export const docsElementLink: TextElementLink = new TextElementLink(
    'https://sigwarth-software.github.io/Crypto-Trader/',
    TargetType.BLANK,
    false,
    'Read Docs',
    TagType.H4,
)

export const upgradeElementLink: ElementLink = new ElementLink('/upgrade', TargetType.SELF, false)

export const consoleElementLink: ElementLink = new ElementLink('/console', TargetType.SELF, false)
