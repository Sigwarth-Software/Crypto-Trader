open module org.cryptotrader.development {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires org.cryptotrader.development.library.config;
    requires org.cryptotrader.development.library.services;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires java.net.http;

    exports org.cryptotrader.development;
}