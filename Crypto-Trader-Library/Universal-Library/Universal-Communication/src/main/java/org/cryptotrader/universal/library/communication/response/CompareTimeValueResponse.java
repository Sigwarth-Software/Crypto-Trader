package org.cryptotrader.universal.library.communication.response;

import lombok.Data;

@Data
public class CompareTimeValueResponse extends TimeValueResponse {
    private double comparedValue;

    public CompareTimeValueResponse(String time, double value, double comparedValue) {
        super(time, value);
        this.comparedValue = comparedValue;
    }
}
