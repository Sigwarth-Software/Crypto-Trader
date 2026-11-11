package org.cryptotrader.testing.library.infrastructure.annotation

import org.junit.jupiter.api.Tag
import org.junit.platform.suite.api.Suite

@Tag("suite")
@Suite(failIfNoTests = false)
annotation class TestSuite
