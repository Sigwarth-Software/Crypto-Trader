open module org.cryptotrader.engine.library.services {
    requires jakarta.annotation;
    requires jakarta.persistence;
    requires static lombok;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.api.library.services;
    requires org.cryptotrader.universal.library.models;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.tx;

    requires spring.core;
    requires spring.aop;

    requires spring.data.jpa;
    requires spring.boot;
    requires org.slf4j;
    requires org.hibernate.orm.core;
    requires spring.security.config;
    requires spring.security.crypto;
    requires spring.security.web;
    requires kotlin.stdlib;
    requires org.cryptotrader.health.library.models;
    requires org.cryptotrader.data.library.models;

    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;
    requires org.jspecify;

    exports org.cryptotrader.engine.library.services;
}
