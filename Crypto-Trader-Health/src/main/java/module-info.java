open module org.cryptotrader.health {
    requires kotlin.stdlib;
    requires org.cryptotrader.health.library.models;
    requires org.cryptotrader.health.library.repositories;
    requires org.cryptotrader.health.library.services;
    requires org.cryptotrader.health.library.components;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.data.jpa;
    requires java.net.http;
    requires org.slf4j;
    requires org.yaml.snakeyaml;
    requires spring.aop;
    requires spring.core;

    exports org.cryptotrader.health;
}
