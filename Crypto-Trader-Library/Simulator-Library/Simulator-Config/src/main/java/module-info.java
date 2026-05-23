module org.cryptotrader.simulator.library.config {
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.kotlin;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.cloud.stream;
    requires spring.messaging;
    requires spring.security.config;
    requires spring.security.web;
    requires org.cryptotrader.simulator.library.communication;
    requires org.cryptotrader.simulator.library.services;
    requires org.cryptotrader.universal.library.communication;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.simulator.library.events;
    requires org.slf4j;

    exports org.cryptotrader.simulator.library.config;

    opens org.cryptotrader.simulator.library.config to spring.core, spring.beans, spring.context;
}
