package org.cryptotrader.admin.ui;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.assets.images.icons.fx.IconImageAssets;
import org.cryptotrader.desktop.library.component.ComponentLoader;
import org.cryptotrader.desktop.library.component.config.SpringContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
@Lazy
public class ExitAnchor extends HBox {
    @FXML
    private ImageView anchorImage;

    public ExitAnchor() {
        SpringContext.getBean(ComponentLoader.class).loadWithFxRoot(this, this);
    }

    @FXML
    void initialize() {
        this.anchorImage.setImage(IconImageAssets.EXIT_DOOR_SVG);
        this.anchorImage.setPreserveRatio(true);
        this.anchorImage.setSmooth(true);
        this.anchorImage.setFitWidth(80);
        this.anchorImage.setFitHeight(80);
    }

    @FXML
    private void exit() {
        log.info("Exiting application");
        System.exit(0);
    }
}
