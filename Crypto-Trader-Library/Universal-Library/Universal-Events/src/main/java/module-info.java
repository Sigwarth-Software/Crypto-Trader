module org.cryptotrader.universal.library.events {
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires static lombok;
    requires org.slf4j;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.web;
    requires spring.jdbc;
    requires spring.boot.autoconfigure;
    requires spring.cloud.stream;
    requires spring.integration.core;
    requires spring.messaging;

    requires org.cryptotrader.universal.library.models;
    exports org.cryptotrader.universal.library.events;
    exports org.cryptotrader.universal.library.events.model;
    exports org.cryptotrader.universal.library.events.alias;
    exports org.cryptotrader.universal.library.events.config;
}