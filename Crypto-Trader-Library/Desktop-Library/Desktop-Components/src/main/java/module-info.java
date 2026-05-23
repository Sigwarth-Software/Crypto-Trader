open module org.cryptotrader.desktop.library.components {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires org.slf4j;
    requires kotlin.stdlib;

    exports org.cryptotrader.desktop.library.component;
    exports org.cryptotrader.desktop.library.component.config;
    exports org.cryptotrader.desktop.library.component.model;
    exports org.cryptotrader.desktop.library.component.ui;
}