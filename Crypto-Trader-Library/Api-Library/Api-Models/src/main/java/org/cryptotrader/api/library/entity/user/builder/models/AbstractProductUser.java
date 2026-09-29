package org.cryptotrader.api.library.entity.user.builder.models;

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.entity.user.SubscriptionTier;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/** Class definition for product user entity builders. */
public abstract class AbstractProductUser implements BuilderFactory<ProductUser> {
    public abstract @NotNull AbstractProductUser username(@Nullable String username);

    public abstract @NotNull AbstractProductUser email(@Nullable String email);

    public abstract @NotNull AbstractProductUser safePassword(@NotNull SafePassword safePassword);

    public abstract @NotNull AbstractProductUser safePassword(@NotNull String rawPassword);

    public abstract @NotNull AbstractProductUser portfolio(@Nullable Portfolio portfolio);

    public abstract @NotNull AbstractProductUser profilePicture(
        @Nullable ProfilePicture profilePicture
    );

    public abstract @NotNull AbstractProductUser lastLogin(@Nullable LocalDateTime lastLogin);

    public abstract @NotNull AbstractProductUser subscriptionTier(
        @NotNull SubscriptionTier subscriptionTier
    );
}
