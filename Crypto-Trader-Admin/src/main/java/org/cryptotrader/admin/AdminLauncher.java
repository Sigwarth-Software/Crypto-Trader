package org.cryptotrader.admin;

import javafx.application.Application;

import java.lang.reflect.Method;

public class AdminLauncher {
    private static final String CODECENTRIC_SVG_LOADER_CLASS = "de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory";

    public static void main(String[] args) {
        attemptInitSvgFactory();
        Application.launch(AdminApplication.class, args);
    }

    private static void attemptInitSvgFactory() {
        try {

            final Class<?> svgLoaderClass = Class.forName(CODECENTRIC_SVG_LOADER_CLASS);
            final Method method = svgLoaderClass.getMethod("install");
            method.invoke(null);
        } catch (final Throwable throwable) {
            final String throwableClassName = throwable.getClass().getName();
            final String throwableMessage = throwable.getMessage();
            // Warn because this is non-fatal.
            System.err.printf("[WARN] SVG loader not installed: %s: %s%n", throwableClassName, throwableMessage);
        }
    }
}
