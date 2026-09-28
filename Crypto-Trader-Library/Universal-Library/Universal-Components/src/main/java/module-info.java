module org.cryptotrader.universal.library.components {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires static lombok;
    requires static org.slf4j;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.web;
    requires spring.jdbc;
    requires kotlin.stdlib;
    requires spring.boot.autoconfigure;
    requires spring.cloud.stream;
    requires spring.integration.core;
    requires spring.messaging;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.universal.library.component;
}