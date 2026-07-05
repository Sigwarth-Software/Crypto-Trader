open module org.cryptotrader.engine {
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.aop;

    requires org.cryptotrader.api.library.infrastructure;
    requires spring.boot.autoconfigure;
    requires spring.data.jpa;
    requires spring.boot;
    requires static lombok;
    requires org.slf4j;
    requires org.cryptotrader.api.library.services;
    requires org.cryptotrader.api.library.events;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.engine.library.services;
    requires org.cryptotrader.logging.library.config;
    requires spring.security.config;
    requires spring.security.crypto;
    requires spring.security.web;
    requires org.cryptotrader.security.library.config;
    requires kotlin.stdlib;
    requires org.cryptotrader.health.library.models;
    requires java.net.http;
    requires spring.cloud.stream;

    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.logging.library.events;

    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;
    requires spring.tx;

    exports org.cryptotrader.engine;
}
