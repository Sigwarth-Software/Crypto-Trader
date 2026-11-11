export type Environment = {
    production: boolean;
    environmentName: string;
    apiUrl: string;
    assetsUrl: string;
    websocketUrl: string;
    enableDebug: boolean;
    featureFlag: boolean;
    persistDpopKey: boolean;
    logging: LoggingEnvironment;
}

export type LoggingEnvironment = {
    level: number;
    serverLogLevel: number;
    serverLoggingUrl: string;
    enableSourceMaps: boolean;
    enableDarkTheme: boolean;
    colorScheme: string[];
}