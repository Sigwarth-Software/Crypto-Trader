open module org.cryptotrader.logging.library.repositories {
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires jakarta.persistence;
    requires static lombok;
    requires org.cryptotrader.logging.library.models;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.logging.library.repository;
}
