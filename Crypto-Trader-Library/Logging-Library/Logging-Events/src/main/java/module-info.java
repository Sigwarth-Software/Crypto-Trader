open module org.cryptotrader.logging.library.events {
    requires kotlin.stdlib;
    requires spring.context;
    requires spring.beans;
    requires org.slf4j;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.api.library.models;

    exports org.cryptotrader.logging.library.events;
    exports org.cryptotrader.logging.library.events.publisher;
}
