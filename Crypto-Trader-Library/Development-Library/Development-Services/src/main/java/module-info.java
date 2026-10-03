module org.cryptotrader.development.library.services {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires spring.context;
    requires spring.beans;
    requires org.kohsuke.github.api;
    requires org.cryptotrader.development.library.models;

    exports org.cryptotrader.development.library.services;
}
