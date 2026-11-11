open module org.cryptotrader.chat.library.services {
    requires kotlin.stdlib;
    requires spring.context;
    requires spring.beans;
    requires spring.webflux;
    requires spring.web;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.chat.library.models;
    requires org.cryptotrader.universal.library.services;
    requires reactor.core;
    requires org.cryptotrader.chat.library.repository;

    exports org.cryptotrader.chat.library.services;
    exports org.cryptotrader.chat.library.services.entity;
}
