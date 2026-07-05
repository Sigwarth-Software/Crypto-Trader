open module org.cryptotrader.api.library.models {
    requires static com.fasterxml.jackson.annotation;
    requires static jakarta.persistence;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires static lombok;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.security.crypto;
    requires spring.web;
    requires org.hibernate.orm.core;
    requires org.cryptotrader.data.library.models;
    requires transitive org.cryptotrader.universal.library.models;
    requires spring.security.core;

    exports org.cryptotrader.api.library.entity.portfolio;
    exports org.cryptotrader.api.library.entity.portfolio.builder;
    exports org.cryptotrader.api.library.entity.portfolio.builder.models;
    exports org.cryptotrader.api.library.entity.trade;
    exports org.cryptotrader.api.library.entity.user;
    exports org.cryptotrader.api.library.entity.user.admin;
    exports org.cryptotrader.api.library.entity.user.builder;
    exports org.cryptotrader.api.library.entity.user.builder.models;
    exports org.cryptotrader.api.library.entity.vendor;

    exports org.cryptotrader.api.library.model.trade;
    exports org.cryptotrader.api.library.model.dpop;
    exports org.cryptotrader.api.library.model.jwt;
    exports org.cryptotrader.api.library.model.portfolio;
}