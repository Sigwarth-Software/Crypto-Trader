package org.cryptotrader.universal.library.communication.response;

import lombok.Data;

@Data
public class CompareTimeValueResponse extends TimeValueResponse {
    private double comparedValue;

    public CompareTimeValueResponse(final String time, final double value, final double comparedValue) {
        super(time, value);
        this.comparedValue = comparedValue;
    }
}
