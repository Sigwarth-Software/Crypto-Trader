package org.cryptotrader.health

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite
import org.junit.jupiter.api.DisplayName
import org.junit.platform.suite.api.SelectPackages

@TestSuite
@SelectPackages("org.cryptotrader.health")
@DisplayName("Crypto-Trader-Health")
class HealthTestSuite
