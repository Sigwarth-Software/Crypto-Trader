open module org.cryptotrader.assets {
    requires javafx.graphics;
    requires javafxsvg;
    requires kotlin.stdlib;

    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.aop;
    requires spring.web;
    requires spring.webmvc;
    requires spring.security.config;
    requires spring.security.web;
    requires spring.tx;
    requires spring.data.jpa;
    requires jakarta.persistence;
    requires org.cryptotrader.assets.library.config;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.assets;
    exports org.cryptotrader.assets.util;
    exports org.cryptotrader.assets.images.icons.fx;
    exports org.cryptotrader.assets.images.logos.cryptotrader.fx;
}