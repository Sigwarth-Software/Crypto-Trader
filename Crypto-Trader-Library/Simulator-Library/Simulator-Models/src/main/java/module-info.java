open module org.cryptotrader.simulator.library.models {
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.api.library.models;
    requires jakarta.persistence;
    requires static lombok;

    exports org.cryptotrader.simulator.library.model;
}