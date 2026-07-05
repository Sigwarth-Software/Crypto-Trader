package org.cryptotrader.admin.controller

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import org.cryptotrader.admin.model.UserTableProperties
import org.cryptotrader.desktop.library.component.ui.DataTable
import org.springframework.stereotype.Component

@Component
class UserTableController {

    @FXML
    lateinit var userDataTableDataTable: DataTable<UserTableProperties>

    @FXML
    fun initialize() {
        val usernameCol = TableColumn<UserTableProperties, String>("Username")
        usernameCol.cellValueFactory = PropertyValueFactory("username")

        val emailCol = TableColumn<UserTableProperties, String>("Email")
        emailCol.cellValueFactory = PropertyValueFactory("email")

        this.userDataTableDataTable.setColumns(listOf(usernameCol, emailCol))

        val users = FXCollections.observableArrayList(
            // TODO: Temporary. Remove this.
            UserTableProperties("admin", "admin@cryptotrader.org"),
            UserTableProperties("john_doe", "john@example.com"),
            UserTableProperties("jane_smith", "jane@example.com")
        )
        this.userDataTableDataTable.setItems(users)
    }
}
