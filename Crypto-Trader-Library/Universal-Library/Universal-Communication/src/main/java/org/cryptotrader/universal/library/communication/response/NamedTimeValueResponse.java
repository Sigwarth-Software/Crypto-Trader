package org.cryptotrader.universal.library.communication.response;

import lombok.Data;

@Data
public class NamedTimeValueResponse extends TimeValueResponse {
    private String name;

    public NamedTimeValueResponse() {
        super();
        this.name = "";
    }

    public NamedTimeValueResponse(final String name, final String timestamp, final double value) {
        super(timestamp, value);
        this.name = name;
    }
}
