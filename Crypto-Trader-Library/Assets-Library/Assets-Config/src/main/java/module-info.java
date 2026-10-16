open module org.cryptotrader.assets.library.config {
    requires org.apache.httpcomponents.httpclient;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.cloud.stream;
    requires spring.context;
    requires spring.security.config;
    requires spring.security.crypto;
    requires spring.security.web;
    requires spring.web;
    requires spring.webmvc;
    requires spring.integration.core;
    requires spring.messaging;
    requires spring.core;
    requires spring.boot;
    requires org.jetbrains.annotations;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires spring.aop;
    requires spring.tx;
    requires jakarta.xml.bind;
    requires jakarta.activation;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires org.yaml.snakeyaml;

    requires spring.jdbc;


    requires org.apache.httpcomponents.httpcore;

    requires spring.security.core;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;

    exports org.cryptotrader.assets.library.config;
}