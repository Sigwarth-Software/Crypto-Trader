package org.cryptotrader.api.library.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cryptotrader.api.library.entity.portfolio.Portfolio;
import org.cryptotrader.api.library.entity.user.builder.ProductUserBuilder;
import org.cryptotrader.universal.library.model.annotation.Loggable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/** The entity for a commercial user of the Crypto Trader app. */
@Entity
@Table(name = "product_users")
@Getter
@Setter
public class ProductUser extends User implements UserDetails {
    //============================-Variables-=================================
    @Loggable
    @Column(name = "email")
    private @Nullable String email;
    @JsonManagedReference
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "portfolio_id", referencedColumnName = "id")
    private @Nullable Portfolio portfolio;
    @OneToOne
    @JoinColumn(name = "profile_picture_id")
    private ProfilePicture profilePicture;
    @Loggable
    @Enumerated(EnumType.STRING)
    @Column(
        name = "subscription_tier",
        nullable = false,
        columnDefinition = "varchar(255) default 'FREE'"
    )
    private SubscriptionTier subscriptionTier;

    //===========================-Constructors-===============================
    public ProductUser() {
        super();
        this.username = null;
        this.email = null;
        this.safePassword = null;
        this.portfolio = null;
        this.lastLogin = null;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username, final String rawPassword) {
        super(username, rawPassword);
        this.email = null;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username,
                       final String rawPassword,
                       final String email) {
        super(username, rawPassword);
        this.email = email;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username,
                       final SafePassword encodedPassword) {
        super(username, encodedPassword);
        this.email = null;
        this.portfolio = new Portfolio(this);
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username,
                       final String email,
                       final SafePassword encodedPassword) {
        super(username, encodedPassword);
        this.email = email;
        this.portfolio = new Portfolio(this);
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username,
                       final String email,
                       final SafePassword encodedPassword,
                       final Portfolio portfolio,
                       final ProfilePicture profilePicture,
                       final LocalDateTime lastLogin,
                       final SubscriptionTier subscriptionTier) {
        super(username, encodedPassword);
        this.email = email;
        this.portfolio = portfolio;
        this.profilePicture = profilePicture;
        this.lastLogin = lastLogin;
        this.subscriptionTier = subscriptionTier;
    }

    public ProductUser(final String username,
                       final String rawPassword,
                       final Portfolio portfolio) {
        super(username, rawPassword);
        this.email = null;
        this.portfolio = portfolio;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public ProductUser(final String username,
                       final String rawPassword,
                       final Portfolio portfolio,
                       final LocalDateTime lastLogin) {
        super(username, rawPassword);
        this.email = null;
        this.portfolio = portfolio;
        this.lastLogin = lastLogin;
        this.subscriptionTier = SubscriptionTier.FREE;
    }

    public static @NotNull ProductUserBuilder builder() {
        return new ProductUserBuilder();
    }

    @JsonIgnore
    @Override
    public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        if (this.safePassword == null) {
            return null;
        }
        return this.safePassword.getEncodedPassword();
    }


    //============================-Overrides-=================================

    //------------------------------Equals------------------------------------

    //------------------------------Hash-Code---------------------------------

    //------------------------------To-String---------------------------------
}
