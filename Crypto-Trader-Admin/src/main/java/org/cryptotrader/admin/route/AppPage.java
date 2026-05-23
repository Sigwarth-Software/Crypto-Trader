package org.cryptotrader.admin.route;

public enum AppPage {
    ADMIN_USERS("Admin Users"),
    AUTH("Authorize"),
    DASHBOARD("Dashboard"),
    EMAIL("Email"),
    TABLES("Tables"),
    TABLE_USER_DATA("User Data Table"),
    TABLE_BAN_OFFENSES("Ban Offenses Table"),
    TABLE_WARN_OFFENSES("Warn Offenses Table");
    public final String pageName;
    AppPage(String pageName) {
        this.pageName = pageName;
    }
}
