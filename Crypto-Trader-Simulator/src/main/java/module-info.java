open module org.cryptotrader.simulator {
    requires kotlin.stdlib;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires org.cryptotrader.simulator.library.config;
    requires org.cryptotrader.simulator.library.services;
    requires org.cryptotrader.simulator.library.events;
    requires org.cryptotrader.logging.library.config;
    requires org.cryptotrader.universal.library.config;
    requires org.cryptotrader.security.library.config;
    requires org.cryptotrader.data.library.repositories;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.events;
    requires spring.data.jpa;

    requires org.yaml.snakeyaml;
    requires spring.web;
    requires jakarta.xml.bind;
    requires spring.cloud.stream;

    exports org.cryptotrader.simulator;
}
