package org.cryptotrader.api;

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectPackages;

@TestSuite
@SelectPackages({"org.cryptotrader.api"})
@DisplayName("Crypto-Trader-Api")
public class ApiTestSuite { }
