open module org.cryptotrader.logging.library.config {
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.logging.library.events;
    requires org.cryptotrader.logging.library.models;
    requires org.cryptotrader.universal.library.events;
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.aop;
    requires spring.cloud.stream;
    requires spring.core;
    requires spring.web;
    requires spring.messaging;
    requires spring.websocket;
    requires org.apache.tomcat.embed.core;
    requires org.aspectj.weaver;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires static lombok;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;
    requires org.cryptotrader.logging.library.scripts;

    exports org.cryptotrader.logging.config;
    exports org.cryptotrader.logging.http;
    exports org.cryptotrader.logging.logback;
    exports org.cryptotrader.logging.properties;
    exports org.cryptotrader.logging.redaction;
    exports org.cryptotrader.logging.websocket;
    exports org.cryptotrader.logging.config.aspect;
}
