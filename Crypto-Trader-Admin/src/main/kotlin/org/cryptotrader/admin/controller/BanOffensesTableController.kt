package org.cryptotrader.admin.controller

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import org.cryptotrader.admin.library.model.BanOffense
import org.cryptotrader.desktop.library.component.ui.DataTable
import org.springframework.stereotype.Component

@Component
class BanOffensesTableController {

    @FXML
    lateinit var banOffensesDataTable: DataTable<BanOffense>

    @FXML
    fun initialize() {
        val nameCol = TableColumn<BanOffense, String>("Offense Name")
        nameCol.cellValueFactory = PropertyValueFactory("nameString")

        val descriptionCol = TableColumn<BanOffense, String>("Description")
        descriptionCol.cellValueFactory = PropertyValueFactory("description")

        banOffensesDataTable.setColumns(listOf(nameCol, descriptionCol))
        banOffensesDataTable.setItems(FXCollections.observableArrayList(*BanOffense.entries.toTypedArray()))
    }
}
