open module org.cryptotrader.security.library.events {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.context;
    requires spring.beans;
    requires org.cryptotrader.security.library.services;
    requires org.cryptotrader.security.library.models;
    requires org.cryptotrader.universal.library.services;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;
    requires com.fasterxml.jackson.annotation;

    exports org.cryptotrader.security.library.event;
    exports org.cryptotrader.security.library.event.publisher;
}
