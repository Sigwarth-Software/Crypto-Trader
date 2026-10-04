module org.cryptotrader.version.library.model {
    requires static lombok;
    requires org.jdom2;
    requires org.slf4j;
    requires java.xml;
    requires kotlin.stdlib;
    requires org.jetbrains.annotations;

    exports org.cryptotrader.version.library.model.config;
    exports org.cryptotrader.version.library.model.dependency;
    exports org.cryptotrader.version.library.model.dependency.type;
    exports org.cryptotrader.version.library.model.module;
    exports org.cryptotrader.version.library.model.module.type;
    exports org.cryptotrader.version.library.model.element;
}