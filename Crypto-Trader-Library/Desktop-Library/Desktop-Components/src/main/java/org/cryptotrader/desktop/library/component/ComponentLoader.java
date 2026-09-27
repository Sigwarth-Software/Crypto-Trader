package org.cryptotrader.desktop.library.component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.cryptotrader.desktop.library.component.config.SpringContext;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Component
public class ComponentLoader {
    public void loadWithFxRoot(final Object controller, final Parent root) {
        final String fxmlPath = this.resolveFxmlPath(controller.getClass());
        final URL resource = Objects.requireNonNull(controller.getClass().getResource(fxmlPath), "FXML not found: " + fxmlPath);

        final AutowireCapableBeanFactory acb = SpringContext.getContext().getAutowireCapableBeanFactory();
        acb.autowireBean(controller);

        final FXMLLoader loader = new FXMLLoader(resource);
        loader.setRoot(root);
        loader.setControllerFactory(type ->
                type.isInstance(controller) ? controller : SpringContext.getContext().getBean(type));
        try {
            loader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load FXML for " + controller.getClass().getName(), ex);
        }
        final String cssPath = fxmlPath.replace(".fxml", ".css");
        final URL css = controller.getClass().getResource(cssPath);
        if (css != null) {
            root.getStylesheets().add(css.toExternalForm());
        }
    }

    public Parent loadAsChild(final Object controller) {
        final String fxmlPath = this.resolveFxmlPath(controller.getClass());
        final URL resource = Objects.requireNonNull(controller.getClass().getResource(fxmlPath), "FXML not found: " + fxmlPath);

        final FXMLLoader loader = new FXMLLoader(resource);
        loader.setController(controller);
        loader.setControllerFactory(type ->
                type.isInstance(controller) ? controller : SpringContext.getContext().getBean(type));

        try {
            final Parent root = loader.load();
            final String cssPath = fxmlPath.replace(".fxml", ".css");
            final URL css = controller.getClass().getResource(cssPath);
            if (css != null) {
                root.getStylesheets().add(css.toExternalForm());
            }
            return root;
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load FXML for " + controller.getClass().getName(), e);
        }
    }

    public void loadIntoPane(final Object controller, final Pane container) {
        final Parent child = loadAsChild(controller);
        container.getChildren().setAll(child);
    }

    private String resolveFxmlPath(final Class<?> componentClass) {
        String packageName = componentClass.getPackageName();
        if (!packageName.contains(".ui")) {
            throw new IllegalArgumentException("Component must be in a '.ui' package: " + packageName);
        }
        final String simpleName = componentClass.getSimpleName();
        final String kebabFolder = simpleName.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
        packageName = packageName.replace('.', '/').replace("/ui", "/ui/component");
        return "/" + packageName + "/" + kebabFolder + "/" + simpleName + ".fxml";
    }
}
