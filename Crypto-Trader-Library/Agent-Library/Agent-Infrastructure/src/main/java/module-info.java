open module org.cryptotrader.agent.library.infrastructure {
    requires jdk.unsupported;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.databind;
    requires org.aspectj.weaver;
    requires org.slf4j;
    requires spring.beans;
    requires spring.context;
    requires spring.security.core;
    requires org.cryptotrader.agent.library.models;
    requires org.cryptotrader.agent.library.services;
    requires org.cryptotrader.universal.library.models;
    requires org.cryptotrader.universal.library.services;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.security.library.infrastructure;

    exports org.cryptotrader.agent.library.infrastructure;
    exports org.cryptotrader.agent.library.infrastructure.aspect;
}
