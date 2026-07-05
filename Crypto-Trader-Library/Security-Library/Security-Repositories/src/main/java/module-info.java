open module org.cryptotrader.security.library.repositories {
    requires kotlin.stdlib;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires spring.tx;
    requires jakarta.persistence;
    requires org.cryptotrader.security.library.models;

    exports org.cryptotrader.security.library.repository;
    exports org.cryptotrader.security.library.repository.keyset;
}
