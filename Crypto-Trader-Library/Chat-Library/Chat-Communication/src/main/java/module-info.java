open module org.cryptotrader.chat.library.communication {
    requires kotlin.stdlib;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.chat.library.models;
    requires spring.security.core;

    exports org.cryptotrader.chat.library.communication.request;
    exports org.cryptotrader.chat.library.communication.response;
}
