open module org.cryptotrader.console.library.events {
    requires kotlin.stdlib;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires org.cryptotrader.console.library.communication;
    requires org.slf4j;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.console.library.components;
    requires org.cryptotrader.console.library.services;
    requires org.cryptotrader.universal.library.events;

    exports org.cryptotrader.console.library.events;
}