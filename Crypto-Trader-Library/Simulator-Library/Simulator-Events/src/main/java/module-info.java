module org.cryptotrader.simulator.library.events {
    requires kotlin.stdlib;
    requires spring.context;
    requires org.slf4j;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.simulator.library.communication;
    requires org.cryptotrader.simulator.library.services;
    requires org.cryptotrader.universal.library.events;

    exports org.cryptotrader.simulator.library.events;
    exports org.cryptotrader.simulator.library.events.publisher;

    opens org.cryptotrader.simulator.library.events to spring.core, spring.beans, spring.context;
}