open module org.cryptotrader.console.library.models {
    requires kotlin.stdlib;
    requires jakarta.persistence;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;
    requires jakarta.annotation;
    requires static lombok;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.console.library.model;
    exports org.cryptotrader.console.library.model.exception;
    exports org.cryptotrader.console.library.model.command;
    exports org.cryptotrader.console.library.entity;
}