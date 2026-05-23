open module org.cryptotrader.admin {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
/*    requires org.reactfx; */
    requires fr.brouillard.oss.cssfx;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.web;
    requires java.net.http;
    requires spring.jdbc;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires java.sql;
    requires net.rgielen.fxweaver.core;
    requires static lombok;
    requires reactfx;
    requires javafx.graphics;
    requires javafx.base;
    requires jakarta.annotation;
    requires jakarta.xml.bind;
    requires org.cryptotrader.assets;
    requires javafxsvg;
    requires org.scenicview.scenicview;
    requires org.cryptotrader.desktop.library.components;

    requires org.cryptotrader.api.library.services;
    requires org.cryptotrader.api.library.repositories;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.api.library.communication;
    requires org.cryptotrader.api.library.events;
    requires org.cryptotrader.api.library.config;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.admin.library.models;
    requires spring.data.jpa;
    requires org.apache.tomcat.embed.core;

    exports org.cryptotrader.admin;
    exports org.cryptotrader.admin.controller;
    exports org.cryptotrader.admin.event;
    exports org.cryptotrader.admin.component;
    exports org.cryptotrader.admin.route;
    exports org.cryptotrader.admin.ui;
    exports org.cryptotrader.admin.model;
}
