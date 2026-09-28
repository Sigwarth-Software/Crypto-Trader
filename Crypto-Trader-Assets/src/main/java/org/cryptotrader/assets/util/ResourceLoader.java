package org.cryptotrader.assets.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {
    public @Nullable InputStream asResourceStream(final @NotNull String url) {
        return this.getClass().getResourceAsStream(url);
    }

    public @Nullable URL asResource(final @NotNull String url) {
        return this.getClass().getResource(url);
    }
}
