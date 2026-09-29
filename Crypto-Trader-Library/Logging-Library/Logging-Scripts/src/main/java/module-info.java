module org.cryptotrader.logging.library.scripts {
    requires kotlin.stdlib;
    requires spring.boot;
    requires spring.core;
    requires spring.web;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.logging.library.communication;
    requires org.cryptotrader.logging.library.events;

    exports org.cryptotrader.logging.library.scripts;
}
