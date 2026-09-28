package org.cryptotrader.desktop.library.component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Component
@Slf4j
public class ViewLoader {
    private final ApplicationContext applicationContext;

    @Autowired
    public ViewLoader(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Parent initializeView(final @NotNull Class<?> controllerClass) {
        final String fxmlPath = this.resolveFxmlPath(controllerClass);
        final String cssPath = fxmlPath.replace(".fxml", ".css");
        final URL resource = Objects.requireNonNull(controllerClass.getResource(fxmlPath));
        final URL cssResource = controllerClass.getResource(cssPath);
        final FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(this.applicationContext::getBean);
        try {
            final Parent load = loader.load();
            if (cssResource != null) {
                load.getStylesheets().add(cssResource.toExternalForm());
            }
            return load;
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to load FXML: " + fxmlPath, exception);
        }
    }

    public void loadView(final Pane container,
                         final @NotNull Class<?> controllerClass) {
        final Parent view = this.initializeView(controllerClass);
        try {
            if (container instanceof final @NotNull VBox vbox) {
                javafx.scene.layout.VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
            } else if (container instanceof javafx.scene.layout.AnchorPane) {
                AnchorPane.setTopAnchor(view, 0.0);
                AnchorPane.setBottomAnchor(view, 0.0);
                AnchorPane.setLeftAnchor(view, 0.0);
                AnchorPane.setRightAnchor(view, 0.0);
            }
        } catch (final Throwable ignored) {

        }
        container.getChildren().setAll(view);
    }

    private @NotNull String resolveFxmlPath(final @NotNull Class<?> controllerClass) {
        final String simpleName = controllerClass.getSimpleName();
        final String basePackage = extractBasePackage(controllerClass, simpleName);
        final String rawFeature = simpleName.substring(0, simpleName.length() - "Controller".length());
        final String feature = toKebabCase(rawFeature);
        final String viewName = simpleName.replace("Controller", "View") + ".fxml";
        final String resourcePackage = basePackage + ".ui.view." + feature;
        final String resourcePath = "/" + resourcePackage.replace('.', '/') + "/" + viewName;
        return resourcePath;
    }

    private static @NotNull String extractBasePackage(final @NotNull Class<?> controllerClass, final @NotNull String simpleName) {
        if (!isValidClass(simpleName)) {
            throw new IllegalArgumentException("Controller class must end with 'Controller': " + simpleName);
        }
        final String packageName = controllerClass.getPackageName();
        final int controllerIndex = packageName.lastIndexOf(".controller");
        if (controllerIndex < 0) {
            throw new IllegalArgumentException("Package must contain '.controller': " + packageName);
        }
        final String basePackage = packageName.substring(0, controllerIndex);
        return basePackage;
    }

    private static @NotNull String toKebabCase(final @Nullable String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        final StringBuilder kebabString = new StringBuilder();
        final char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char letter = chars[i];
            final boolean isUpper = Character.isUpperCase(letter);
            if (i > 0 && isUpper) {
                final boolean hasLowerNeighbor = Character.isLowerCase(chars[i - 1]) || (i + 1 < chars.length && Character.isLowerCase(chars[i + 1]));
                if (hasLowerNeighbor) {
                    kebabString.append('-');
                }
            }
            kebabString.append(Character.toLowerCase(letter));
        }
        return kebabString.toString();
    }

    private static boolean isValidClass(final @NotNull String simpleName) {
        return simpleName.endsWith("Controller");
    }
}
