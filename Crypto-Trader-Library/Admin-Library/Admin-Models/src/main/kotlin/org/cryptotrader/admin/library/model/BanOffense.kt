package org.cryptotrader.admin.library.model

const val baseMessage: String = "You have been banned. Your assets will no longer" +
        " be traded. You may can no longer use Crypto Trader in any " +
        "capacity. Contact support for guidance. If you believe this " +
        "to be in error, please contact support immediately."

enum class BanOffense(val description: String) {
    BOT("Your account has been detected as a bot. $baseMessage"),
    SPAM("Your account has been detected as spamming. $baseMessage"),
    ILLEGAL("Your account has been for illegal activities. $baseMessage"),
    HACKING("Your account has been detected as hacking. $baseMessage");

    val nameString: String get() = name
}