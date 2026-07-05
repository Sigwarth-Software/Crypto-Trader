open module org.cryptotrader.console.library.components {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires java.net.http;
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
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.extensions;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.console.library.communication;
    requires transitive org.cryptotrader.universal.library.events;
    requires org.cryptotrader.api.library.services;

    requires java.sql;
    requires spring.aop;
    requires spring.tx;
    requires org.apache.tomcat.embed.core;
    requires jjwt.api;
    requires com.auth0.jwt;
    requires spring.data.commons;
    requires org.cryptotrader.api.library.communication;
    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.data.library.components;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.api.library.models;

    exports org.cryptotrader.console.library.component;
}