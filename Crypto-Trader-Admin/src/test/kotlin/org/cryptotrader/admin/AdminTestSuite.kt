package org.cryptotrader.admin

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite
import org.junit.jupiter.api.DisplayName
import org.junit.platform.suite.api.SelectPackages

@TestSuite
@SelectPackages("org.cryptotrader.admin")
@DisplayName("Crypto-Trader-Admin")
class AdminTestSuite
