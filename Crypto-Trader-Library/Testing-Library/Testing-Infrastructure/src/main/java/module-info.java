module org.cryptotrader.testing.library.infrastructure {
    requires kotlin.stdlib;
    requires org.slf4j;
    requires org.junit.jupiter.api;
    requires org.junit.platform.suite.api;
    requires org.jetbrains.annotations;
    requires org.mockito.junit.jupiter;

    exports org.cryptotrader.testing.library.infrastructure;
    exports org.cryptotrader.testing.library.infrastructure.annotation;
    exports org.cryptotrader.testing.library.infrastructure.logging;
}
