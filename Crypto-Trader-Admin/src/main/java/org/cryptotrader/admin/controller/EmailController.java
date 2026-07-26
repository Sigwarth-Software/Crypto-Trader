package org.cryptotrader.admin.controller;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.springframework.stereotype.Component;

@Component
public class EmailController extends VBox {
    // TODO: This should be a property. Not hardcoded.
    private static final String WEBMAIL_URL = "https://sscryptotrader.awsapps.com/mail";

    @FXML
    private WebView webView;

    public EmailController() {

    }

    @FXML
    private void initialize() {
        // Let the WebView grow and shrink within its parent
        VBox.setVgrow(this.webView, Priority.ALWAYS);
        this.webView.setMinSize(0, 0);

        final Parent webViewParent = this.webView.getParent();
        if (webViewParent instanceof final Region parent) {
            this.webView.prefWidthProperty().bind(parent.widthProperty());
            this.webView.prefHeightProperty().bind(parent.heightProperty());
        }

        this.webView.getEngine().load(WEBMAIL_URL);
    }
}
