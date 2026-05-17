open module org.cryptotrader.logging.library.config {
    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.web;
    requires spring.messaging;
    requires spring.websocket;
    requires org.apache.tomcat.embed.core;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires static lombok;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;

    exports org.cryptotrader.logging.config;
    exports org.cryptotrader.logging.http;
    exports org.cryptotrader.logging.logback;
    exports org.cryptotrader.logging.properties;
    exports org.cryptotrader.logging.websocket;
}
