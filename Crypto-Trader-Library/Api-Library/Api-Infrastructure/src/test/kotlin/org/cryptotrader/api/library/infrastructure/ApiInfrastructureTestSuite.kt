package org.cryptotrader.api.library.infrastructure

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite
import org.junit.jupiter.api.DisplayName
import org.junit.platform.suite.api.SelectPackages

@TestSuite
@SelectPackages("org.cryptotrader.api.library.infrastructure")
@DisplayName("Api-Infrastructure")
class ApiInfrastructureTestSuite
