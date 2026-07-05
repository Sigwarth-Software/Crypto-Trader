open module org.cryptotrader.security.library.infrastructure {
    requires kotlin.stdlib;
    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires org.apache.tomcat.embed.core;
    requires org.cryptotrader.security.library.services;
    requires org.slf4j;

    exports org.cryptotrader.security.library.infrastructure;
    exports org.cryptotrader.security.library.infrastructure.config;
    exports org.cryptotrader.security.library.infrastructure.annotation;
}
