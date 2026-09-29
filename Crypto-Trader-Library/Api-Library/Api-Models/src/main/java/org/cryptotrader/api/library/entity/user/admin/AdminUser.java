package org.cryptotrader.api.library.entity.user.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.cryptotrader.api.library.entity.user.SafePassword;
import org.cryptotrader.api.library.entity.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The user entity for admin users. */
@Entity
@Table(name = "admin_users")
@Getter
@Setter
public class AdminUser extends User {
    public AdminUser() {
        super();
    }

    public AdminUser(@Nullable final String username,
                     @NotNull  final String rawPassword) {
        super(username, rawPassword);
    }

    public AdminUser(@Nullable final String username,
                     @NotNull final SafePassword encodedPassword) {
        super(username, encodedPassword);
    }
}
