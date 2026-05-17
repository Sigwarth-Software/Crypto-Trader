module org.cryptotrader.universal.library.communication {
    requires kotlin.stdlib;
    requires static lombok;
    requires static com.fasterxml.jackson.annotation;
    requires org.cryptotrader.universal.library.models;

    exports org.cryptotrader.universal.library.communication.response;
}