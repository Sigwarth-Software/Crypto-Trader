open module org.cryptotrader.security.library.events {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.context;
    requires org.cryptotrader.security.library.services;
    requires org.cryptotrader.security.library.models;
    requires org.cryptotrader.universal.library.services;
    requires org.cryptotrader.universal.library.models;

    exports org.cryptotrader.security.library.event;
}
