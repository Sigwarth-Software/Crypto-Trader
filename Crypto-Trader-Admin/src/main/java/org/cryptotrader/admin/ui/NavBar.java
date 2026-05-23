package org.cryptotrader.admin.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.desktop.library.component.ComponentLoader;
import org.cryptotrader.desktop.library.component.config.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
@Lazy
public class NavBar extends HBox {
    @FXML
    private HomeAnchor homeAnchor;

    @FXML
    private ExitAnchor exitAnchor;

    @FXML
    private NavItem authButton;

    @FXML
    private NavItem adminButton;

    @FXML
    private NavItem tablesButton;

    @Autowired
    private ApplicationEventPublisher events;

    public NavBar() {
        SpringContext.getBean(ComponentLoader.class).loadWithFxRoot(this, this);
    }

    @FXML
    public void initialize() {

    }
}
