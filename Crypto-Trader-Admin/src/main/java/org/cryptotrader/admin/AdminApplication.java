package org.cryptotrader.admin;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import de.codecentric.centerdevice.javafxsvg.dimension.PrimitiveDimensionProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.cryptotrader.admin.dev.JRebelHook;
import org.cryptotrader.assets.images.logos.cryptotrader.fx.CryptoTraderLogoImageAssets;
import org.scenicview.ScenicView;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.cryptotrader.admin.config.SpringBootConfig;
import fr.brouillard.oss.cssfx.CSSFX;


import java.io.IOException;
import java.net.URL;

public class AdminApplication extends Application {
    private static final String ROOT_APP_VIEW_PATH = "ui/view/app/AppView.fxml";
    private static final String ROOT_APP_STYLESHEET_PATH = "ui/view/app/AppView.css";
    private static final String APP_PANEL_TITLE = "Crypto Trader Admin Panel";

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        System.setProperty("spring.aop.proxy-target-class", "false");
        this.applicationContext = new SpringApplicationBuilder(SpringBootConfig.class)
                                      .headless(false)
                                      .run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(AdminApplication.class.getResource(ROOT_APP_VIEW_PATH));
        fxmlLoader.setControllerFactory(this.applicationContext::getBean);
        final Scene scene = new Scene(fxmlLoader.load(), 900, 700);
        stage.setTitle(APP_PANEL_TITLE);
        final URL cssPath = AdminApplication.class.getResource(ROOT_APP_STYLESHEET_PATH);
        addPossibleStylesheet(cssPath, scene);
        stage.setScene(scene);
        CSSFX.start();
        stage.show();
        ScenicView.show(scene);
        addDesktopIcon(stage);
        this.addReloadKeybind(scene);
        this.addJRebelListener(scene);
    }

    private static void addPossibleStylesheet(URL cssPath, Scene scene) {
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath.toExternalForm());
        }
    }

    private static void addDesktopIcon(Stage stage) {
        stage.getIcons().add(CryptoTraderLogoImageAssets.CROPPED_TRANSPARENT_PNG);
    }

    private void addJRebelListener(Scene scene) {
        JRebelHook.register(
                () -> reloadScene(scene),
                "org.cryptotrader.admin.controller",
                "org.cryptotrader.admin.ui"
        );
    }

    private void addReloadKeybind(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R,
                KeyCombination.CONTROL_DOWN,
                KeyCombination.SHIFT_DOWN), () -> reloadScene(scene));
    }


    public void reloadScene(Scene scene) {
        try {
            FXMLLoader reloadLoader = new FXMLLoader(
                    AdminApplication.class.getResource("ui/view/app/AppView.fxml"));
            reloadLoader.setControllerFactory(this.applicationContext::getBean);

            Parent newRoot = reloadLoader.load();
            scene.setRoot(newRoot);
            // TODO: Add a property or profile which triggers ScenicView.
//            ScenicView.show(newRoot);
            final int controllerIdentityHash = System.identityHashCode(reloadLoader.getController());
            System.out.printf("[Hot Reload] scene root swapped, controller = %s @%d%n", reloadLoader.getController(), controllerIdentityHash);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
        this.applicationContext.close();
    }
}
