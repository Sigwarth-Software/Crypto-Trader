package org.cryptotrader.version

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite
import org.junit.jupiter.api.DisplayName
import org.junit.platform.suite.api.SelectPackages

@TestSuite
@SelectPackages("org.cryptotrader.version")
@DisplayName("Crypto-Trader-Version")
class VersionTestSuite
