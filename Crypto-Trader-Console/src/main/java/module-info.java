open module org.cryptotrader.console {
    requires kotlin.stdlib;

    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.aop;
    requires spring.web;
    requires spring.tx;
    requires spring.data.jpa;

    requires jakarta.persistence;
    requires jakarta.xml.bind;
    requires jakarta.activation;

    requires spring.security.config;
    requires spring.security.web;
    requires spring.security.crypto;


    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires org.apache.httpcomponents.httpclient;

    requires spring.jdbc;

    requires org.slf4j;

    requires org.apache.httpcomponents.httpcore;

    requires org.hibernate.orm.core;


    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;

    requires spring.webmvc;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.events;
    requires io.swagger.v3.oas.annotations;
    requires org.cryptotrader.docs;

    requires spring.security.core;
    requires com.auth0.jwt;
    requires org.cryptotrader.universal.library.models;
    requires spring.data.commons;


    requires org.cryptotrader.console.library.communication;
    requires org.cryptotrader.console.library.components;
    requires org.cryptotrader.console.library.models;
    requires org.cryptotrader.console.library.services;
    requires org.cryptotrader.console.library.events;
    requires org.cryptotrader.console.library.repositories;

    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.data.library.components;
    requires org.cryptotrader.security.library.config;

    exports org.cryptotrader.console;
}
