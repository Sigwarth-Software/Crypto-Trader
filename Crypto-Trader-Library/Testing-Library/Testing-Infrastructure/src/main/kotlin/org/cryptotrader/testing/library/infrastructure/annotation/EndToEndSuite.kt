package org.cryptotrader.testing.library.infrastructure.annotation

import org.junit.jupiter.api.Tag

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Tag("e2e")
annotation class EndToEndSuite
