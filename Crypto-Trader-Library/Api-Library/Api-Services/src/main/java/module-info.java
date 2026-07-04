open module org.cryptotrader.api.library.services {
    requires java.sql;
    requires static lombok;
    requires org.cryptotrader.api.library.communication;
    requires org.cryptotrader.api.library.components;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.api.library.repositories;
    requires org.cryptotrader.api.library.events;
    requires org.cryptotrader.api.library.scripts;
    requires spring.beans;
    requires spring.context;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.web;
    requires spring.boot.autoconfigure;
    requires org.apache.tomcat.embed.core;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires jjwt.api;
    requires com.auth0.jwt;
    requires spring.data.commons;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.universal.library.extensions;
    requires org.cryptotrader.universal.library.services;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.api.library.services.jwt;
    exports org.cryptotrader.api.library.services;
    exports org.cryptotrader.api.library.services.entity.portfolio;
    exports org.cryptotrader.api.library.services.entity.trade;
    exports org.cryptotrader.api.library.services.entity.user;
    exports org.cryptotrader.api.library.services.dpop;
    exports org.cryptotrader.api.library.services.rsa;
    exports org.cryptotrader.api.library.services.models;
}