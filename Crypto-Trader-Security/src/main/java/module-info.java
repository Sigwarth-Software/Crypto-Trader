open module org.cryptotrader.security {
    requires jakarta.persistence;
    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.security.web;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.data.jpa;
    requires org.cryptotrader.security.library.config;
    requires org.cryptotrader.security.library.events;
    requires org.cryptotrader.security.library.services;
    requires org.cryptotrader.security.library.repositories;
    requires org.cryptotrader.security.library.models;
    requires spring.messaging;
    requires spring.cloud.stream;
    requires org.slf4j;
    requires static lombok;
    requires com.google.crypto.tink;
    requires inet.ipaddr;
    requires org.cryptotrader.api.library.models;
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.config;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.universal.library.services;

    exports org.cryptotrader.security;
}
