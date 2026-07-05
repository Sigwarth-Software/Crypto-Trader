package org.cryptotrader.admin.library.model

const val warnMessage: String = "This is just a warning. Repeated offenses will result in a ban."

enum class WarnOffense(val description: String) {
    ADMIN_ACCESS("You have tried to access admin panel with a non-admin account. $warnMessage");

    val nameString: String get() = name
}