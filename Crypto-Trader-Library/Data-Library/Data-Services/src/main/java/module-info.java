open module org.cryptotrader.data.library.services {
    requires static lombok;
    requires spring.beans;
    requires spring.context;

    requires spring.boot.autoconfigure;
    requires spring.data.jpa;
    requires spring.boot;
    requires java.sql;
    requires spring.web;
    requires org.slf4j;
    requires spring.core;
    requires spring.aop;
    requires spring.tx;
    requires spring.security.config;
    requires spring.security.crypto;
    requires spring.security.web;
    requires spring.webmvc;
    requires kotlin.stdlib;
    requires jakarta.xml.bind;
    requires jakarta.activation;
    requires jakarta.persistence;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.data.library.communication;
    requires org.cryptotrader.universal.library.communication;
    requires org.cryptotrader.data.library.repositories;
    requires spring.data.commons;
    requires org.cryptotrader.data.library.components;
    requires org.cryptotrader.api.library.communication;
    requires spring.jdbc;
    requires jakarta.annotation;
    requires org.cryptotrader.universal.library.services;

    exports org.cryptotrader.data.library.services;
    exports org.cryptotrader.data.library.services.entity;
    exports org.cryptotrader.data.library.services.harvest;
    exports org.cryptotrader.data.library.services.models;
    exports org.cryptotrader.data.library.services.adapter;
}