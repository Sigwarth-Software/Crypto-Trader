open module org.cryptotrader.security.library.services {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires com.google.crypto.tink;
    requires inet.ipaddr;
    requires org.apache.tomcat.embed.core;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires org.cryptotrader.security.library.repositories;
    requires org.cryptotrader.security.library.models;
    requires spring.beans;
    requires spring.tx;
    requires spring.web;
    requires java.net.http;
    requires static lombok;
    requires org.cryptotrader.universal.library.services;
    requires org.cryptotrader.universal.library.models;
    requires spring.aop;
    requires spring.core;

    exports org.cryptotrader.security.library.service;
    exports org.cryptotrader.security.library.service.model;
    exports org.cryptotrader.security.library.service.entity;
}
