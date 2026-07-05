open module org.cryptotrader.universal.library.models {
    requires jakarta.persistence;
    requires static lombok;
    requires spring.web;
    requires static com.fasterxml.jackson.annotation;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.security.crypto;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.universal.library.model;
    exports org.cryptotrader.universal.library.model.annotation;
    exports org.cryptotrader.universal.library.entity;
    exports org.cryptotrader.universal.library.model.http;
    exports org.cryptotrader.universal.library.model.exception;
}
