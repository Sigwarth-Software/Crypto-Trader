package org.cryptotrader.admin.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class AdminUsersController extends VBox {
    @FXML
    private Label welcomeText;
    public AdminUsersController() {

    }

    @FXML
    private void initialize() {
        this.welcomeText.setText("Welcome to Crypto Trader Admin");
    }
}
