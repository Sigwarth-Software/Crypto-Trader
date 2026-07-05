package org.cryptotrader.simulator.library.scripts

fun getNaturalValue(
    currencyValueStart: Double,
    currencyValueEnd: Double,
    currencyShares: Double,
): Double {
    val initialValue = currencyValueStart * currencyShares
    val finalValue = currencyValueEnd * currencyShares
    return finalValue - initialValue
}

fun getCryptoValue(
    currencyValueStart: Double,
    currencySharesStart: Double,
    currencyValueEnd: Double,
    currencySharesEnd: Double,
): Double {
    val initialValue = currencyValueStart * currencySharesStart
    val finalValue = currencyValueEnd * currencySharesEnd
    return finalValue - initialValue
}