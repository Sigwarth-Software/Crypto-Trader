module org.cryptotrader.logging.library.scripts {
    requires kotlin.stdlib;
    requires spring.boot;
    requires spring.core;
    requires spring.web;
    requires org.apache.tomcat.embed.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.universal.library.scripts;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.logging.library.communication;
    requires org.cryptotrader.logging.library.events;

    exports org.cryptotrader.logging.library.scripts;
}
