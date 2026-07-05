import { Injectable } from '@angular/core'
import { environment } from '@environments/environment'
import { Observable } from 'rxjs'

import { HttpClientService } from '@theoliverlear/angular-suite'
import { ConsoleCommandRequest, ConsoleCommandResponse } from '@models/console/types'
import { CryptoTraderLoggerService } from '@services/logging/crypto-trader-logger.service'
import { LoggerContext } from '@models/logging/LoggerContext'
import { resolveAbsoluteHttpUrl } from '@app/scripts/url-resolver.script'

/** A service for executing console commands by sending HTTP requests.
 *
 */
@Injectable({
    providedIn: 'root',
})
export class ConsoleCommandService extends HttpClientService<
    ConsoleCommandRequest,
    ConsoleCommandResponse
> {
    private static readonly URL: string = resolveAbsoluteHttpUrl(
        `${environment.apiUrl}/console/execute`,
    )

    constructor(private readonly log: CryptoTraderLoggerService) {
        super(ConsoleCommandService.URL)
        this.log.setContext(LoggerContext.Console)
    }

    /** Executes a console command by sending a POST request to the server
     *  with the command text.
     *
     * @param command
     */
    public executeCommand(command: ConsoleCommandRequest): Observable<ConsoleCommandResponse> {
        this.log.info(`Executing: ${command.commandText}`)
        return this.post(command)
    }
}
