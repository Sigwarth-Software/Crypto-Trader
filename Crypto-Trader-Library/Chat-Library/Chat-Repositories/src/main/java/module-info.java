open module org.cryptotrader.chat.library.repository {
    requires org.cryptotrader.chat.library.models;
    requires kotlin.stdlib;
    requires spring.data.jpa;

    requires spring.data.commons;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires jakarta.persistence;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.chat.library.repository;
}