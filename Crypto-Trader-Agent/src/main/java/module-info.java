module org.cryptotrader.agent {
    requires kotlin.stdlib;
    requires kotlin.reflect;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.web;
    requires spring.webmvc;
    requires com.fasterxml.jackson.databind;
    requires org.cryptotrader.agent.library.components;
    requires org.cryptotrader.agent.library.config;
    requires org.cryptotrader.universal.library.config;
    requires org.cryptotrader.security.library.config;
    requires org.cryptotrader.development.library.config;
    requires java.net.http;
    requires jakarta.xml.bind;
    requires jakarta.activation;

    exports org.cryptotrader.agent;
    opens org.cryptotrader.agent to spring.core, spring.beans, spring.context;
}
