open module org.cryptotrader.chat.library.events {
    requires kotlin.stdlib;
    requires spring.context;
    requires spring.beans;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.chat.library.communication;
    requires org.slf4j;

    exports org.cryptotrader.chat.library.events;
}
