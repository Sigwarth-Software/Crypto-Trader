package org.cryptotrader.universal.library.communication.response;

import lombok.Data;

@Data
public class NamedTimeValueResponse extends TimeValueResponse {
    private String name;

    public NamedTimeValueResponse() {
        super();
        this.name = "";
    }

    public NamedTimeValueResponse(String name, String timestamp, double value) {
        super(timestamp, value);
        this.name = name;
    }
}
