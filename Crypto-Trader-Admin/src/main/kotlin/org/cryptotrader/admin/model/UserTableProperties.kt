package org.cryptotrader.admin.model

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

// TODO: Create a BaseProperties class. Create a @AdminProperty annotation.
//       Properties within annotation include, field, appropriate type
//       representation, and a display name.
//
//       Use these to mark classes and which will appear in data tables. Use
//       reflection to populate the properties class in appropriate form to
//       display in UI.

class UserTableProperties(username: String, email: String) {
    val usernameProperty: StringProperty = SimpleStringProperty(username)
    val emailProperty: StringProperty = SimpleStringProperty(email)

    var username: String
        get() = this.usernameProperty.get()
        set(value) = this.usernameProperty.set(value)

    var email: String
        get() = this.emailProperty.get()
        set(value) = this.emailProperty.set(value)
}
