open module org.cryptotrader.chat.library.config {
    requires kotlin.stdlib;
    requires spring.context;
    requires spring.beans;
    requires spring.webflux;
    requires spring.web;
    requires org.slf4j;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.chat.library.communication;
    requires org.cryptotrader.chat.library.events;
    requires org.cryptotrader.api.library.models;

    exports org.cryptotrader.chat.library.config;
}