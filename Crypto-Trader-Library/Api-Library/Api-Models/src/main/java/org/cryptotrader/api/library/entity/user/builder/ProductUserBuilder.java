package org.cryptotrader.api.library.entity.user.builder;

import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.user.ProductUser;
import org.cryptotrader.api.library.entity.user.ProfilePicture;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.entity.user.SubscriptionTier;
import org.cryptotrader.api.library.entity.user.builder.models.AbstractProductUser;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/** A builder factory for product user entities. */
public class ProductUserBuilder extends AbstractProductUser {
    private String username;
    private String email;
    private SafePassword safePassword;
    private Portfolio portfolio;
    private ProfilePicture profilePicture;
    private LocalDateTime lastLogin;
    private SubscriptionTier subscriptionTier;

    public ProductUserBuilder() {
        this.username = null;
        this.email = null;
        this.safePassword = null;
        this.portfolio = null;
        this.lastLogin = null;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    @Override
    public @NotNull AbstractProductUser username(final String username) {
        this.username = username;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser email(final String email) {
        this.email = email;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser safePassword(@NotNull final SafePassword safePassword) {
        this.safePassword = safePassword;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser safePassword(@NotNull final String rawPassword) {
        this.safePassword = new SafePassword(rawPassword);
        return this;
    }

    @Override
    public @NotNull AbstractProductUser portfolio(final Portfolio portfolio) {
        this.portfolio = portfolio;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser profilePicture(final ProfilePicture profilePicture) {
        this.profilePicture = profilePicture;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser lastLogin(final LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }

    @Override
    public @NotNull AbstractProductUser subscriptionTier(
        @NotNull final SubscriptionTier subscriptionTier
    ) {
        this.subscriptionTier = subscriptionTier;
        return this;
    }

    @Override
    public @NotNull ProductUser build() {
        return new ProductUser(this.username,
                        this.email,
                        this.safePassword,
                        this.portfolio,
                        this.profilePicture,
                        this.lastLogin,
                        this.subscriptionTier);
    }
}
