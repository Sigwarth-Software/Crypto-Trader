open module org.cryptotrader.data.library.components {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires static lombok;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.cryptotrader.data.library.communication;
    requires org.cryptotrader.data.library.models;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.jdbc;
    requires spring.web;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.universal.library.models;
    requires org.slf4j;

    exports org.cryptotrader.data.library.component;
}