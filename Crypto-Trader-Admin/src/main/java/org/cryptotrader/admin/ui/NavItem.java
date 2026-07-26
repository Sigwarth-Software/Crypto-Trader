package org.cryptotrader.admin.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.admin.event.PageNavigationEvent;
import org.cryptotrader.desktop.library.component.ComponentLoader;
import org.cryptotrader.desktop.library.component.config.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.cryptotrader.admin.route.AppPage;

@Slf4j
@Component
@Scope("prototype")
@Lazy
public class NavItem extends HBox {
    @FXML
    private Button actionButton;

    private final ObjectProperty<AppPage> page = new SimpleObjectProperty<>();
    public NavItem() {
        SpringContext.getBean(ComponentLoader.class).loadWithFxRoot(this, this);
    }


    @Autowired
    private ApplicationEventPublisher events;

    @FXML
    public void initialize() {
        this.actionButton.textProperty().bind(Bindings.createStringBinding(() -> {
            final AppPage enumPage = this.page.get();
            return enumPage == null ? "" : enumPage.pageName;
        }, this.page));
    }

    @FXML
    public void navigate() {
        log.info("Navigating to {}", this.page.get());
        this.events.publishEvent(new PageNavigationEvent(this.page.get()));
    }

    public AppPage getPage() {
        return page.get();
    }

    public ObjectProperty<AppPage> pageProperty() {
        return page;
    }

    public void setPage(AppPage page) {
        this.page.set(page);
    }
}
