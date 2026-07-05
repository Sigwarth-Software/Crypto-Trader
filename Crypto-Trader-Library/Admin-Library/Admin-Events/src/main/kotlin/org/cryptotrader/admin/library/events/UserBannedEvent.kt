package org.cryptotrader.admin.library.events

import org.cryptotrader.admin.library.model.BanOffense
import org.cryptotrader.api.library.entity.user.User

data class UserBannedEvent(
    val user: User,
    val offense: BanOffense
)
