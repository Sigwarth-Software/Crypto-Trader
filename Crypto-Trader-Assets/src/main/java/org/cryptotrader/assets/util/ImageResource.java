package org.cryptotrader.assets.util;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

public class ImageResource extends LoadableResource {
    public ImageResource() {
        super();
    }

    public @NotNull Image getImage(final String url) {
        return new Image(this.resourceLoader.asResourceStream(url));
    }
}
