open module org.cryptotrader.agent.library.services {
    requires kotlin.stdlib;
    requires org.cryptotrader.agent.library.models;
    requires org.cryptotrader.agent.library.repository;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.universal.library.services;
    requires spring.beans;
    requires spring.context;
    requires spring.data.commons;
    requires spring.data.jpa;

    exports org.cryptotrader.agent.library.services.entity;
}
