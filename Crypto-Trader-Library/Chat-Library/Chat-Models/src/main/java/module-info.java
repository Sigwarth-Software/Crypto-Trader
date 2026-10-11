open module org.cryptotrader.chat.library.models {
    requires kotlin.stdlib;
    requires jakarta.persistence;
    requires org.cryptotrader.universal.library.models;
    requires org.jetbrains.annotations;
    requires static lombok;

    exports org.cryptotrader.chat.library.entity;
    exports org.cryptotrader.chat.library.model;
}