package org.cryptotrader.assets.util;

import org.jetbrains.annotations.NotNull;

public class LoadableResource {
    protected final @NotNull ResourceLoader resourceLoader;

    public LoadableResource() {
        this.resourceLoader = new ResourceLoader();
    }
}
