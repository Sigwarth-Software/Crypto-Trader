package org.cryptotrader.api.library.entity.user.builder;

import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.api.library.entity.user.builder.models.AbstractProfilePicture;
import org.jetbrains.annotations.NotNull;

/** A factory builder for creating profile picture entities. */
public class ProfilePictureBuilder extends AbstractProfilePicture {
    private String fileName;
    private String fileType;
    private byte[] fileData;
    private ProductUser user;

    public ProfilePictureBuilder() {
        super();
        this.fileName = "";
        this.fileData = new byte[0];
        this.user = null;
    }

    @Override
    public @NotNull ProfilePictureBuilder fileName(final String fileName) {
        this.fileName = fileName;
        this.fileType = ProfilePicture.getFileType(fileName);
        return this;
    }

    @Override
    public @NotNull AbstractProfilePicture fileType(final String fileType) {
        this.fileType = fileType;
        return this;
    }

    @Override
    public @NotNull AbstractProfilePicture fileData(final byte[] fileData) {
        this.fileData = fileData;
        return this;
    }

    @Override
    public @NotNull AbstractProfilePicture user(final ProductUser user) {
        this.user = user;
        return this;
    }

    @Override
    public @NotNull ProfilePicture build() {
        return new ProfilePicture(this.fileName, this.fileData, this.user);
    }
}
