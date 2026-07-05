open module org.cryptotrader.api.library.config {
    requires org.apache.httpcomponents.httpclient;
    requires org.cryptotrader.universal.library.config;
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
    requires org.cryptotrader.security.library.services;
    requires org.cryptotrader.api.library.infrastructure;
    requires spring.aop;
    requires spring.tx;
    requires spring.data.jpa;

    requires jakarta.persistence;
    requires jakarta.xml.bind;
    requires jakarta.activation;


    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires org.yaml.snakeyaml;

    requires spring.jdbc;


    requires org.apache.httpcomponents.httpcore;

    requires org.hibernate.orm.core;

    requires spring.security.core;
    requires com.auth0.jwt;
    requires spring.data.commons;
    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;


    exports org.cryptotrader.api.library.config;
}