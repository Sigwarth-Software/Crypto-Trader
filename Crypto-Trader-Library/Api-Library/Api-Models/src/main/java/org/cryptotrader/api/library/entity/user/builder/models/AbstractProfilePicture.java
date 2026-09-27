package org.cryptotrader.api.library.entity.user.builder.models;

import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Class definition for profile picture entity builders. */
public abstract class AbstractProfilePicture implements BuilderFactory<ProfilePicture> {
    public abstract @NotNull AbstractProfilePicture fileName(@Nullable String fileName);

    public abstract @NotNull AbstractProfilePicture fileType(@Nullable String fileType);

    public abstract @NotNull AbstractProfilePicture fileData(byte[] fileData);

    public abstract @NotNull AbstractProfilePicture user(@Nullable ProductUser user);
}
