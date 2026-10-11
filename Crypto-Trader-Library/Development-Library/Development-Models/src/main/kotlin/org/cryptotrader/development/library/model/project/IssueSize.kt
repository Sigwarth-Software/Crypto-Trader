package org.cryptotrader.development.library.model.project

enum class IssueSize(val sizeName: String) {
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL");

    companion object {
        fun from(sizeName: String): IssueSize? {
            return entries.find { it.sizeName.equals(sizeName, ignoreCase = true) }
        }
    }
}