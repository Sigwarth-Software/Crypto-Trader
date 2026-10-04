module org.cryptotrader.contact.library.models {
    requires kotlin.stdlib;
    requires jakarta.persistence;
    requires org.cryptotrader.api.library.models;
    requires static lombok;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires org.cryptotrader.universal.library.models;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.contact.library.entity;
    exports org.cryptotrader.contact.library.entity.builder;
    exports org.cryptotrader.contact.library.entity.builder.models;
}