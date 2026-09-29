package org.cryptotrader.api.library.communication.response;

import lombok.Data;

@Data
public class OperationSuccessfulResponse {
    boolean successful;

    public OperationSuccessfulResponse(final boolean successful) {
        this.successful = successful;
    }
}
