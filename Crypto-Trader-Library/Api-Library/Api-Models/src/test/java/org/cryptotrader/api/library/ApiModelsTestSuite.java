package org.cryptotrader.api.library;

import org.cryptotrader.testing.library.infrastructure.annotation.TestSuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectPackages;

@TestSuite
@SelectPackages({"org.cryptotrader.api.library.entity",
                 "org.cryptotrader.api.library.model"})

@DisplayName("Api-Models")
public class ApiModelsTestSuite { }
