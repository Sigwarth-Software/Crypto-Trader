package org.cryptotrader.api.library.communication.request;
//=================================-Imports-==================================
import lombok.Data;

@Data
public class UserRequest {
    //============================-Variables-=================================
    private String username;
    private String email;
    private String password;
    //============================-Constructors-==============================
    public UserRequest(final String username,
                       final String email,
                       final String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
