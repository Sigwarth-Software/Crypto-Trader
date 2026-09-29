package org.cryptotrader.contact.library.entity.builder.models;

import org.cryptotrader.contact.library.entity.CryptoTraderMailer;
import org.cryptotrader.contact.library.entity.Email;
import org.cryptotrader.contact.library.entity.EmailType;
import org.cryptotrader.universal.library.model.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** The class that represents an email entity builder. */
public abstract class AbstractEmail implements BuilderFactory<Email> {
    public abstract @NotNull AbstractEmail cryptoTraderMailer(@NotNull CryptoTraderMailer mailer);

    public abstract @NotNull AbstractEmail toAddress(@Nullable String toAddress);

    public abstract @NotNull AbstractEmail subject(@Nullable String subject);

    public abstract @NotNull AbstractEmail body(@Nullable String body);

    public abstract @NotNull AbstractEmail type(@NotNull EmailType type);

    public abstract @NotNull Email build();
}
