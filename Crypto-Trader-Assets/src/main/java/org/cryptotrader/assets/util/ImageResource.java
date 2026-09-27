package org.cryptotrader.assets.util;

import javafx.scene.image.Image;

public class ImageResource extends LoadableResource {
    public ImageResource() {
        super();
    }

    public Image getImage(final String url) {
        return new Image(this.resourceLoader.asResourceStream(url));
    }
}
