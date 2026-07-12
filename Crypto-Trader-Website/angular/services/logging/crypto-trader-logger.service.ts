import { Injectable } from '@angular/core';
import { Logger as TsLogger } from 'tslog';
import { LogLayer, type LogLayerPlugin } from 'loglayer';
import { TsLogTransport } from '@loglayer/transport-tslog';
import { HttpTransport } from '@loglayer/transport-http';
import { redactionPlugin } from '@loglayer/plugin-redaction';
import { serializeError } from 'serialize-error';
import { environment } from '@environments/environment';
import {LoggerContext} from "@models/logging/LoggerContext";

// TODO: Move to utils.
function formatTimestamp(date: Date): string {
    const options: Intl.DateTimeFormatOptions = {
        timeZone: 'America/Chicago',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
    };
    const parts = new Intl.DateTimeFormat('en-US', options).formatToParts(date);
    const get = (type: string): string => parts.find((p): boolean => p.type === type)!.value;
    return `${get('month')}/${get('day')}/${get('year')} - ${get('hour')}:${get('minute')}:${get('second')}`;
}

// TODO: Move to assets.
const prettyLogger = new TsLogger({
    name: 'System',
    type: 'pretty',
    minLevel: 1, // Trace
    stylePrettyLogs: true,
    prettyLogTemplate:
        '{{mm}}/{{dd}}/{{yyyy}} - {{hh}}:{{MM}}:{{ss}}\t{{logLevelName}}\t{{nameWithDelimiterSuffix}}\n',
    prettyErrorTemplate: '\n{{errorName}} {{errorMessage}}\n{{errorStack}}',
    prettyErrorStackTemplate: '  at {{fileNameWithLine}} {{method}}\n',
    prettyLogStyles: {
        yyyy: 'dim',
        mm: 'dim',
        dd: 'dim',
        hh: 'dim',
        MM: 'dim',
        ss: 'dim',
        logLevelName: {
            '*': ['bold', 'white'],
            SILLY: ['bold', 'white'],
            TRACE: ['bold', 'magenta'],
            DEBUG: ['bold', 'cyan'],
            INFO: ['bold', 'green'],
            WARN: ['bold', 'yellow'],
            ERROR: ['bold', 'red'],
            FATAL: ['bold', 'redBright', 'bgBlack'],
        },
        name: ['bold', 'blueBright'],
        nameWithDelimiterPrefix: ['bold', 'blueBright'],
        nameWithDelimiterSuffix: ['bold', 'blueBright'],
    },
});

/** Logger which provides client logging and host capturing.
 *
 */
@Injectable({ providedIn: 'root' })
export class CryptoTraderLoggerService {
    private readonly logger: LogLayer;
    private context: string = 'System';

    constructor() {
        this.logger = new LogLayer({
            errorSerializer: serializeError,
            transport: [
                new TsLogTransport({
                    logger: prettyLogger,
                }),
                new HttpTransport({
                    url: environment.logging.serverLoggingUrl || '/api/logs/website',
                    headers: () => ({
                        'content-type': 'application/json',
                        'x-client-app': 'crypto-trader-website',
                    }),
                    payloadTemplate: ({ logLevel, message, data, error }) =>
                        JSON.stringify({
                            timestamp: formatTimestamp(new Date()),
                            level: logLevel,
                            logger: 'frontend',
                            context: data?.['context'] || this.context,
                            message,
                            metadata: data,
                            error: error
                                ? {
                                      name: error.name,
                                      message: error.message,
                                      stack: error.stack,
                                  }
                                : undefined,
                        }),
                    enableBatchSend: true,
                    batchSize: 50,
                    batchSendTimeout: 3000,
                    contentType: 'application/json',
                    batchContentType: 'application/x-ndjson',
                }),
            ],
            plugins: [
                redactionPlugin({
                    paths: ['password', 'token', 'authorization', 'cookie'],
                }) as LogLayerPlugin,
            ],
        });
    }

    // TODO: Make an enum.
    /** Sets the context of the logger.
     *
     * @param context
     */
    public setContext(context: LoggerContext): void {
        this.context = context;
        prettyLogger.settings.name = context;
    }

    /** Gets the context of the logger.
     *
     */
    public getContext(): string {
        return this.context;
    }

    /** Logs a trace message.
     *
     * @param message
     * @param metadataOrContext
     */
    public trace(message: string | object, metadataOrContext?: Record<string, unknown> | string): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        this.getChain(this.getMetadata(metadataOrContext)).trace(message);
    }

    /** Logs a debug message.
     *
     * @param message
     * @param metadataOrContext
     */
    public debug(message: string | object, metadataOrContext?: Record<string, unknown> | string): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        this.getChain(this.getMetadata(metadataOrContext)).debug(message);
    }

    /** Logs an info message.
     *
     * @param message
     * @param metadataOrContext
     */
    public info(message: string | object, metadataOrContext?: Record<string, unknown> | string): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        this.getChain(this.getMetadata(metadataOrContext)).info(message);
    }

    /** Logs a log message.
     *
     * @param message
     * @param metadataOrContext
     */
    public log(message: string | object, metadataOrContext?: Record<string, unknown> | string): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        this.getChain(this.getMetadata(metadataOrContext)).info(message);
    }

    /** Logs a warning message.
     *
     * @param message
     * @param metadataOrContext
     */
    public warn(message: string | object, metadataOrContext?: Record<string, unknown> | string): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        this.getChain(this.getMetadata(metadataOrContext)).warn(message);
    }

    /** Logs an error message.
     *
     * @param message
     * @param error
     * @param metadataOrContext
     */
    public error(
        message: string | object,
        error?: Error,
        metadataOrContext?: Record<string, unknown> | string,
    ): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        let chain: any = this.getChain(this.getMetadata(metadataOrContext));
        if (error) {
            chain = chain.withError(error);
        }
        chain.error(message);
    }

    /** Logs a fatal error message.
     *
     * @param message
     * @param error
     * @param metadataOrContext
     */
    public fatal(
        message: string | object,
        error?: Error,
        metadataOrContext?: Record<string, unknown> | string,
    ): void {
        if (typeof message === 'object') {
            message = JSON.stringify(message, null, 2);
        }
        let chain: any = this.getChain(this.getMetadata(metadataOrContext));
        if (error) {
            chain = chain.withError(error);
        }
        chain.fatal(message);
    }

    private getMetadata(
        metadataOrContext?: Record<string, unknown> | string,
    ): Record<string, unknown> | undefined {
        if (typeof metadataOrContext === 'string') {
            return { context: metadataOrContext };
        }
        return metadataOrContext;
    }

    // TODO: Call with generic instead of any.
    private getChain(metadata?: Record<string, unknown>): any {
        let chain: any = this.logger;
        if (metadata) {
            chain = chain.withMetadata(metadata);
        }
        return chain;
    }
}
