open module org.cryptotrader.security.library.models {
    requires static jakarta.persistence;
    requires static lombok;
    requires kotlin.stdlib;
    requires transitive org.cryptotrader.api.library.models;
    requires org.cryptotrader.universal.library.models;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.security.library.entity.ip;
    exports org.cryptotrader.security.library.entity.key;
    exports org.cryptotrader.security.library.model;
    exports org.cryptotrader.security.library.model.key;
    exports org.cryptotrader.security.library.entity.keyset;
}
