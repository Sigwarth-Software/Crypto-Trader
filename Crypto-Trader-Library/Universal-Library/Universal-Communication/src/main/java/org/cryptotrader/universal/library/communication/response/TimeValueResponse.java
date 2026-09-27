package org.cryptotrader.universal.library.communication.response;

import lombok.Data;

@Data
public class TimeValueResponse {
    private String timestamp;
    private double value;

    public TimeValueResponse() {
        this.timestamp = "";
        this.value = 0;
    }

    public TimeValueResponse(final String timestamp, final double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
}