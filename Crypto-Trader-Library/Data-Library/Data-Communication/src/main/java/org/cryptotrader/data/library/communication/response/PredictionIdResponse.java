package org.cryptotrader.data.library.communication.response;

import lombok.Data;

@Data
public class PredictionIdResponse {
    private Long predictionId;
    public PredictionIdResponse(final Long predictionId) {
        this.predictionId = predictionId;
    }
}