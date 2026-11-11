open module org.cryptotrader.chat.library.models {
    requires kotlin.stdlib;
    requires jakarta.persistence;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;
    requires org.jetbrains.annotations;
    requires static lombok;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.chat.library.entity;
    exports org.cryptotrader.chat.library.model;
}