open module org.cryptotrader.agent.library.models {
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.models;
    requires jakarta.persistence;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires com.fasterxml.jackson.databind;
    requires org.hibernate.orm.core;

    exports org.cryptotrader.agent.library.entity;
    exports org.cryptotrader.agent.library.model;
    exports org.cryptotrader.agent.library.model.database;
    exports org.cryptotrader.agent.library.model.file;
}
