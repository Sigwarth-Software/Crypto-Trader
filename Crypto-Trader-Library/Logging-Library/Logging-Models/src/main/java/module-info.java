open module org.cryptotrader.logging.library.models {
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires kotlin.stdlib;
    requires static lombok;

    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.api.library.models;

    exports org.cryptotrader.logging.library.entity;
}
