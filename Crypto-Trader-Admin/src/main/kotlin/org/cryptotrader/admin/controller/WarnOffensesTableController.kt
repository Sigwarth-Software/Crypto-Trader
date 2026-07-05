package org.cryptotrader.admin.controller

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import org.cryptotrader.admin.library.model.WarnOffense
import org.cryptotrader.desktop.library.component.ui.DataTable
import org.springframework.stereotype.Component

@Component
class WarnOffensesTableController {

    @FXML
    lateinit var warnOffensesDataTable: DataTable<WarnOffense>

    @FXML
    fun initialize() {
        val nameCol = TableColumn<WarnOffense, String>("Offense Name")
        nameCol.cellValueFactory = PropertyValueFactory("nameString")

        val descriptionCol = TableColumn<WarnOffense, String>("Description")
        descriptionCol.cellValueFactory = PropertyValueFactory("description")

        warnOffensesDataTable.setColumns(listOf(nameCol, descriptionCol))
        warnOffensesDataTable.setItems(FXCollections.observableArrayList(*WarnOffense.values()))
    }
}
