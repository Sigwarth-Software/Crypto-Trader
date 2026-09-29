package org.cryptotrader.api.library.communication.response;

import lombok.Data;

@Data
public class HasProfilePictureResponse {
    boolean hasProfilePicture;
    public HasProfilePictureResponse(final boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }
}
