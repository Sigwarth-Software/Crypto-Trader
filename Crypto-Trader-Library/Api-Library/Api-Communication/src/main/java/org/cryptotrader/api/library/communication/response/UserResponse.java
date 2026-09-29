package org.cryptotrader.api.library.communication.response;
//=================================-Imports-==================================
import lombok.Data;

@Data
public class UserResponse {
    //============================-Variables-=================================
    private String message;
    //===========================-Constructors-===============================
    public UserResponse(final String message) {
        this.message = message;
    }
}
