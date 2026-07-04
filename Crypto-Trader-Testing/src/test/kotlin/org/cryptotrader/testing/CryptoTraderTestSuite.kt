package org.cryptotrader.testing

import org.junit.platform.suite.api.ExcludeClassNamePatterns
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite

@Suite
@SelectPackages("org.cryptotrader")
@ExcludeClassNamePatterns(".*Suite")
class CryptoTraderTestSuite
