module org.cryptotrader.testing.library.infrastructure {
    requires kotlin.stdlib;
    requires info.picocli;
    requires org.slf4j;
    requires org.junit.jupiter.api;
    requires org.junit.platform.launcher;
    requires org.junit.platform.engine;
    requires org.junit.platform.suite.api;
    requires org.jetbrains.annotations;
    requires org.mockito.junit.jupiter;

    provides org.junit.platform.launcher.TestExecutionListener with
        org.cryptotrader.testing.library.infrastructure.logging.HierarchicalTestReporter;

    exports org.cryptotrader.testing.library.infrastructure;
    exports org.cryptotrader.testing.library.infrastructure.annotation;
    exports org.cryptotrader.testing.library.infrastructure.logging;
}
