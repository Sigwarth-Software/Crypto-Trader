module org.cryptotrader.simulator.library.services {
    requires kotlin.stdlib;
    requires java.sql;

    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.api.library.communication;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.data.library.repositories;
    requires org.cryptotrader.simulator.library.scripts;
    requires org.cryptotrader.simulator.library.models;
    requires org.cryptotrader.simulator.library.communication;
    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.data.library.components;
    requires org.cryptotrader.universal.library.communication;

    requires spring.boot.starter.data.jpa;
    requires spring.beans;
    requires spring.context;
    requires org.slf4j;

    exports org.cryptotrader.simulator.library.services;
}