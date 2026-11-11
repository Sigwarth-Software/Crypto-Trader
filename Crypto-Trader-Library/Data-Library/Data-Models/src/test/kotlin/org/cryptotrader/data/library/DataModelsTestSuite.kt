package org.cryptotrader.data.library

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite
import org.junit.jupiter.api.DisplayName
import org.junit.platform.suite.api.SelectPackages

@TestSuite
@SelectPackages("org.cryptotrader.data.library.entity",
    "org.cryptotrader.data.library.model")

@DisplayName("Data-Models")
class DataModelsTestSuite
