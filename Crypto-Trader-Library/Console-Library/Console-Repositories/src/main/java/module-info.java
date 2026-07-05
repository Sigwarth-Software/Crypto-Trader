open module org.cryptotrader.console.library.repositories {
    requires org.cryptotrader.console.library.models;
    requires spring.data.jpa;
    requires spring.beans;
    requires static lombok;
    requires spring.context;

    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires java.sql;
    requires spring.web;
    requires org.slf4j;
    requires spring.core;
    requires spring.aop;
    requires spring.tx;

    exports org.cryptotrader.console.library.repository;
}