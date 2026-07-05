module org.cryptotrader.simulator.library.communication {
    requires kotlin.stdlib;
    requires org.cryptotrader.universal.library.communication;
    requires org.cryptotrader.simulator.library.models;

    exports org.cryptotrader.simulator.library.communication.request;
    exports org.cryptotrader.simulator.library.communication.response;

    opens org.cryptotrader.simulator.library.communication.request;
    opens org.cryptotrader.simulator.library.communication.response;
}