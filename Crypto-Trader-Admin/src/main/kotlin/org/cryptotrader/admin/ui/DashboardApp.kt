package org.cryptotrader.admin.ui

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.cryptotrader.admin.route.AppPage
import org.cryptotrader.desktop.library.component.ComponentLoader
import org.cryptotrader.desktop.library.component.config.SpringContext
import javafx.beans.binding.Bindings

class DashboardApp : VBox() {
    val page: ObjectProperty<AppPage> = SimpleObjectProperty()

    @FXML
    private lateinit var appLabel: Label

    init {
        SpringContext.getBean(ComponentLoader::class.java).loadWithFxRoot(this, this)
    }

    @FXML
    fun initialize() {
        this.appLabel.textProperty().bind(
            Bindings.createStringBinding(
                {
                    this.page.get()?.name ?: ""
                }, this.page)
        )
    }
}