open module org.cryptotrader.data.library.models {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires jakarta.persistence;
    requires static lombok;
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.security.crypto;
    requires spring.web;
    requires org.hibernate.orm.core;
    requires transitive org.cryptotrader.universal.library.models;

    exports org.cryptotrader.data.library.entity.currency;
    exports org.cryptotrader.data.library.entity.currency.builder;
    exports org.cryptotrader.data.library.entity.currency.builder.models;
    exports org.cryptotrader.data.library.entity.news;
    exports org.cryptotrader.data.library.entity.news.builder;
    exports org.cryptotrader.data.library.entity.news.builder.models;
    exports org.cryptotrader.data.library.entity.prediction;
    exports org.cryptotrader.data.library.entity.prediction.builder;
    exports org.cryptotrader.data.library.entity.prediction.builder.models;
    exports org.cryptotrader.data.library.entity.training;
    exports org.cryptotrader.data.library.entity.training.specs;
    exports org.cryptotrader.data.library.entity.training.builder;
    exports org.cryptotrader.data.library.entity.training.builder.models;
    exports org.cryptotrader.data.library.model.currency;
    exports org.cryptotrader.data.library.model.http;
}