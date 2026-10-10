open module org.cryptotrader.development.library.services {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.context;
    requires spring.beans;
    requires org.kohsuke.github.api;
    requires org.cryptotrader.development.library.models;
    requires org.cryptotrader.development.library.communication;

    requires spring.web;
    requires spring.core;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.openapitools.jackson.nullable;

    requires jakarta.annotation;

    exports org.cryptotrader.development.library.services.github.generated.model;
    exports org.cryptotrader.development.library.services.github.generated.api;
    exports org.cryptotrader.development.library.services.github.generated.client;

    exports org.cryptotrader.development.library.services;
    exports org.cryptotrader.development.library.services.client;
    exports org.cryptotrader.development.library.services.client.exchange;
}
