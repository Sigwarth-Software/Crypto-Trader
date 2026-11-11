open module org.cryptotrader.health.library.config {
    requires spring.beans;
    requires spring.context;
    requires spring.web;
    requires java.net.http;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires spring.aop;
    requires spring.core;
    requires org.cryptotrader.health.library.services;
    requires org.cryptotrader.health.library.models;
    requires org.cryptotrader.health.library.communication;
    requires org.cryptotrader.health.library.events;
    requires org.cryptotrader.universal.library.events;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.cloud.stream;
    requires spring.messaging;

    exports org.cryptotrader.health.library.config;
    exports org.cryptotrader.health.library.config.consumer;
}
