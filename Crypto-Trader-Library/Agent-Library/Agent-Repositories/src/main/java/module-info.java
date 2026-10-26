module org.cryptotrader.agent.library.repository {
    requires kotlin.stdlib;
    requires org.cryptotrader.agent.library.models;
    requires spring.data.commons;
    requires spring.data.jpa;

    opens org.cryptotrader.agent.library.repository to spring.beans, spring.context, spring.core, spring.data.commons, spring.data.jpa;

    exports org.cryptotrader.agent.library.repository;
}
