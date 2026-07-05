package org.cryptotrader.desktop.library.component.ui

import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.layout.VBox
import org.cryptotrader.desktop.library.component.ComponentLoader
import org.cryptotrader.desktop.library.component.config.SpringContext
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.function.Function

@Component
@Scope("prototype")
@Lazy
class DataTable<T> : VBox() {

    @FXML
    lateinit var tableView: TableView<T>

    init {
        SpringContext.getBean(ComponentLoader::class.java).loadWithFxRoot(this, this)
    }

    fun setColumns(columns: List<TableColumn<T, *>>) {
        if (this::tableView.isInitialized) {
            this.tableView.columns.setAll(columns)
        }
    }

    fun setItems(items: ObservableList<T>) {
        if (this::tableView.isInitialized) {
            this.tableView.items = items
        }
    }

    /**
     * Sets a context menu for the entire table area (useful for empty space or general actions).
     */
    fun setTableContextMenu(contextMenu: ContextMenu) {
        if (this::tableView.isInitialized) {
            this.tableView.contextMenu = contextMenu
        }
    }

    /**
     * Sets a context menu specifically for rows. The factory provides the data item for the clicked row.
     */
    fun setRowContextMenu(contextMenuFactory: Function<T, ContextMenu>) {
        if (this::tableView.isInitialized) {
            this.tableView.setRowFactory {
                val row = TableRow<T>()
                row.itemProperty().addListener { _, _, newItem ->
                    if (newItem != null) {
                        row.contextMenu = contextMenuFactory.apply(newItem)
                    } else {
                        row.contextMenu = null
                    }
                }
                row
            }
        }
    }
}
