package org.cryptotrader.universal.library.model.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class PayloadStatusResponse<T> {
    //=============================-Variables-=================================
    private T payload;
    private HttpStatus status;
    public PayloadStatusResponse(final T payload, final HttpStatus status) {
        this.payload = payload;
        this.status = status;
    }
}
