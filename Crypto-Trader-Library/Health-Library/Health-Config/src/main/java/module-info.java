module org.cryptotrader.health.library.config {
    requires spring.beans;
    requires spring.context;
    requires spring.web;
    requires java.net.http;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires spring.aop;
    requires spring.core;

    exports org.cryptotrader.health.library.config;
}