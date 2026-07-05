open module org.cryptotrader.health.library.services {
    requires org.cryptotrader.health.library.models;
    requires org.cryptotrader.health.library.repositories;
    requires spring.beans;
    requires spring.context;
    requires spring.tx;
    requires spring.web;
    requires java.net.http;
    requires static lombok;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.services;
    requires spring.aop;
    requires spring.core;

    exports org.cryptotrader.health.library.service;
}
