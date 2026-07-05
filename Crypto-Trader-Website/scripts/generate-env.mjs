// Generates Angular environment files at build time so they are not committed.
// Reads values from process.env with sensible defaults.
// This script is intentionally minimal and framework-agnostic.

import { existsSync, mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { config } from 'dotenv';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

config({ path: resolve(__dirname, '..', '.env') });

function boolFromEnv(val, fallback = false) {
    if (val === undefined) return fallback;
    const s = String(val).toLowerCase();
    return s === '1' || s === 'true' || s === 'yes' || s === 'on';
}

const ENV = {
    // Common
    ENVIRONMENT: process.env.ENVIRONMENT || 'development',
    API_URL:
        process.env.WEBSITE_API_URL ||
        process.env.API_URL ||
        '/api',
    WEBSOCKET_URL:
        process.env.WEBSITE_WEBSOCKET_URL ||
        process.env.WEBSOCKET_URL ||
        '/ws',
    ENABLE_DEBUG: boolFromEnv(
        process.env.WEBSITE_ENABLE_DEBUG ?? process.env.ENABLE_DEBUG,
        true,
    ),
    FEATURE_FLAG: boolFromEnv(
        process.env.WEBSITE_FEATURE_FLAG ?? process.env.FEATURE_FLAG,
        false,
    ),
    LOG_LEVEL: parseInt(process.env.WEBSITE_LOG_LEVEL || process.env.LOG_LEVEL || '1'),
    SERVER_LOG_LEVEL: parseInt(process.env.WEBSITE_SERVER_LOG_LEVEL || process.env.SERVER_LOG_LEVEL || '5'),
    SERVER_LOGGING_URL: process.env.WEBSITE_SERVER_LOGGING_URL || process.env.SERVER_LOGGING_URL || '/api/logs/website',
    ENABLE_SOURCE_MAPS: boolFromEnv(process.env.WEBSITE_ENABLE_SOURCE_MAPS || process.env.ENABLE_SOURCE_MAPS, true),
    ENABLE_DARK_THEME: boolFromEnv(process.env.WEBSITE_ENABLE_DARK_THEME || process.env.ENABLE_DARK_THEME, true),
    COLOR_SCHEME: (process.env.WEBSITE_COLOR_SCHEME || process.env.COLOR_SCHEME || 'purple,teal,gray,gray,orange,red,darkred').split(','),
};

const outDir = resolve(__dirname, '..', 'angular', 'environments');
if (!existsSync(outDir)) {
    mkdirSync(outDir, { recursive: true });
}

function fileContent({
    production,
    environmentName,
    apiUrl,
    websocketUrl,
    enableDebug,
    featureFlag,
    persistDpopKey,
    logging,
}) {
    // Produce a TypeScript module exporting the environment object.
    return `import { Environment } from './types';

export const environment: Environment = {
    production: ${production},
    environmentName: ${JSON.stringify(environmentName)},
    apiUrl: ${JSON.stringify(apiUrl)},
    websocketUrl: ${JSON.stringify(websocketUrl)},
    enableDebug: ${enableDebug},
    featureFlag: ${featureFlag},
    persistDpopKey: ${persistDpopKey},
    logging: {
        level: ${logging.level},
        serverLogLevel: ${logging.serverLogLevel},
        serverLoggingUrl: ${JSON.stringify(logging.serverLoggingUrl)},
        enableSourceMaps: ${logging.enableSourceMaps},
        enableDarkTheme: ${logging.enableDarkTheme},
        colorScheme: [${logging.colorScheme.map(c => JSON.stringify(c)).join(', ')}]
    }
};
`;
}

const files = [
    {
        name: 'environment.ts',
        data: fileContent({
            production: false,
            environmentName: ENV.ENVIRONMENT,
            apiUrl: ENV.API_URL,
            websocketUrl: ENV.WEBSOCKET_URL,
            enableDebug: ENV.ENABLE_DEBUG,
            featureFlag: ENV.FEATURE_FLAG,
            persistDpopKey: true,
            logging: {
                level: ENV.LOG_LEVEL,
                serverLogLevel: ENV.SERVER_LOG_LEVEL,
                serverLoggingUrl: ENV.SERVER_LOGGING_URL,
                enableSourceMaps: ENV.ENABLE_SOURCE_MAPS,
                enableDarkTheme: ENV.ENABLE_DARK_THEME,
                colorScheme: ENV.COLOR_SCHEME,
            },
        }),
    },
    {
        name: 'environment.dev.ts',
        data: fileContent({
            production: false,
            environmentName: 'development',
            apiUrl: ENV.API_URL,
            websocketUrl: ENV.WEBSOCKET_URL,
            enableDebug: ENV.ENABLE_DEBUG,
            featureFlag: ENV.FEATURE_FLAG,
            persistDpopKey: true,
            logging: {
                level: ENV.LOG_LEVEL,
                serverLogLevel: ENV.SERVER_LOG_LEVEL,
                serverLoggingUrl: ENV.SERVER_LOGGING_URL,
                enableSourceMaps: ENV.ENABLE_SOURCE_MAPS,
                enableDarkTheme: ENV.ENABLE_DARK_THEME,
                colorScheme: ENV.COLOR_SCHEME,
            },
        }),
    },
    {
        name: 'environment.prod.ts',
        data: fileContent({
            production: true,
            environmentName: 'production',
            apiUrl:
                process.env.WEBSITE_API_URL_PROD ||
                process.env.API_URL_PROD ||
                ENV.API_URL,
            websocketUrl:
                process.env.WEBSITE_WEBSOCKET_URL_PROD ||
                process.env.WEBSOCKET_URL_PROD ||
                ENV.WEBSOCKET_URL,
            enableDebug: boolFromEnv(
                process.env.WEBSITE_ENABLE_DEBUG_PROD ??
                    process.env.ENABLE_DEBUG_PROD,
                false,
            ),
            featureFlag: boolFromEnv(
                process.env.WEBSITE_FEATURE_FLAG_PROD ??
                    process.env.FEATURE_FLAG_PROD,
                true,
            ),
            persistDpopKey: true,
            logging: {
                level: parseInt(process.env.WEBSITE_LOG_LEVEL_PROD || process.env.LOG_LEVEL_PROD || '5'),
                serverLogLevel: parseInt(process.env.WEBSITE_SERVER_LOG_LEVEL_PROD || process.env.SERVER_LOG_LEVEL_PROD || '5'),
                serverLoggingUrl: process.env.WEBSITE_SERVER_LOGGING_URL_PROD || process.env.SERVER_LOGGING_URL_PROD || ENV.SERVER_LOGGING_URL,
                enableSourceMaps: boolFromEnv(process.env.WEBSITE_ENABLE_SOURCE_MAPS_PROD || process.env.ENABLE_SOURCE_MAPS_PROD, false),
                enableDarkTheme: boolFromEnv(process.env.WEBSITE_ENABLE_DARK_THEME_PROD || process.env.ENABLE_DARK_THEME_PROD, true),
                colorScheme: (process.env.WEBSITE_COLOR_SCHEME_PROD || process.env.COLOR_SCHEME_PROD || 'purple,teal,gray,gray,orange,red,darkred').split(','),
            },
        }),
    },
];

for (const file of files) {
    const path = resolve(outDir, file.name);
    writeFileSync(path, file.data, 'utf8');
}

// Optional: emit a brief log for CI debugging
if (boolFromEnv(process.env.VERBOSE_ENV_GEN)) {
    console.log('[env-gen] Generated env files at', outDir);
}
