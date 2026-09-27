package org.cryptotrader.assets.util;

import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {
    public InputStream asResourceStream(final String url) {
        return this.getClass().getResourceAsStream(url);
    }

    public URL asResource(final String url) {
        return this.getClass().getResource(url);
    }
}
