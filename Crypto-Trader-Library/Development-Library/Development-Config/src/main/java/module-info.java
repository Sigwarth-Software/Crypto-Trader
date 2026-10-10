open module org.cryptotrader.development.library.config {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires spring.context;
    requires spring.boot;
    requires org.kohsuke.github.api;
    requires org.cryptotrader.development.library.services;
    requires org.cryptotrader.development.library.models;
    requires org.slf4j;

    exports org.cryptotrader.development.library.config;
}
