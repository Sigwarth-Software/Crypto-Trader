/**
 * Java Platform Module for the Crypto‑Trader HTTP API.
 *
 * Responsibilities:
 * - Boots the Spring application (controllers, filters, services) that expose the REST API and WS endpoints.
 * - Depends on the shared Api‑Library modules for data models, services, infrastructure filters (DPoP/JWT), and config.
 * - Opens the module to Spring so that reflection can proxy/inject where needed ("open module").
 *
 * Notable exports:
 * - org.cryptotrader.api.controller: REST controllers (including auth endpoints and JWKS).
 * - org.cryptotrader.api.controller.websocket: WS endpoints that optionally accept Bearer at upgrade.
 */
open module org.cryptotrader.api {
    requires kotlin.stdlib;
    requires com.sigwarthsoftware.springboot.websocket;

    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.aop;
    requires spring.web;
    requires spring.tx;
    requires spring.data.jpa;

    requires jakarta.persistence;
    requires jakarta.xml.bind;
    requires jakarta.activation;

    requires spring.security.config;
    requires spring.security.web;
    requires spring.security.crypto;


    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires org.apache.httpcomponents.httpclient;

    requires spring.jdbc;

    requires org.slf4j;

    requires org.apache.httpcomponents.httpcore;

    requires org.hibernate.orm.core;

    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.apache.tomcat.embed.websocket;

    requires spring.webmvc;
    requires org.cryptotrader.api.library.models;
    requires org.cryptotrader.api.library.communication;
    requires org.cryptotrader.api.library.services;
    requires org.cryptotrader.api.library.components;
    requires org.cryptotrader.api.library.repositories;
    requires org.cryptotrader.api.library.infrastructure;
    requires org.cryptotrader.api.library.events;
    requires org.cryptotrader.api.library.config;
    requires org.cryptotrader.security.library.config;
    requires org.cryptotrader.universal.library.components;
    requires org.cryptotrader.universal.library.events;
    requires org.cryptotrader.console.library.components;
    requires org.cryptotrader.console.library.communication;
    requires org.cryptotrader.simulator.library.events;
    requires org.cryptotrader.simulator.library.communication;
    requires org.cryptotrader.logging.library.communication;
    requires org.cryptotrader.logging.library.events;
    requires org.cryptotrader.logging.library.config;
    requires org.cryptotrader.security.library.models;
    requires io.swagger.v3.oas.annotations;
    requires org.cryptotrader.docs;

    requires org.cryptotrader.security.library.services;
    requires spring.security.core;
    requires com.auth0.jwt;
    requires org.cryptotrader.api.library.scripts;
    requires org.cryptotrader.api.library.extensions;
    requires org.cryptotrader.data.library.services;
    requires org.cryptotrader.data.library.models;
    requires org.cryptotrader.data.library.components;
    requires org.cryptotrader.universal.library.models;
    requires spring.data.commons;

    exports org.cryptotrader.api;
    exports org.cryptotrader.api.config;
    exports org.cryptotrader.api.controller;
    exports org.cryptotrader.api.controller.websocket;
    exports org.cryptotrader.api.infrastructure;
    exports org.cryptotrader.api.service;
}
