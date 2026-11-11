open module org.cryptotrader.health.library.events {
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.health.library.communication;
    requires spring.context;
    requires spring.beans;

    exports org.cryptotrader.health.library.event;
}
