module org.cryptotrader.universal.library.config {
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires transitive org.apache.httpcomponents.httpclient;
    requires java.net.http;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.universal.library.config;
    opens org.cryptotrader.universal.library.config to spring.core, spring.beans, spring.context;
}
