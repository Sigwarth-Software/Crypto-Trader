package org.cryptotrader.admin.controller

import javafx.fxml.FXML
import org.cryptotrader.admin.event.PageNavigationEvent
import org.cryptotrader.admin.route.AppPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class TablesController {

    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher

    @FXML
    fun onUserDataClick() {
        this.eventPublisher.publishEvent(PageNavigationEvent(AppPage.TABLE_USER_DATA))
    }

    @FXML
    fun onBanOffensesClick() {
        this.eventPublisher.publishEvent(PageNavigationEvent(AppPage.TABLE_BAN_OFFENSES))
    }

    @FXML
    fun onWarnOffensesClick() {
        this.eventPublisher.publishEvent(PageNavigationEvent(AppPage.TABLE_WARN_OFFENSES))
    }
}
