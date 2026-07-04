package org.cryptotrader.testing;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import java.util.Locale;
import java.util.Map;

// TODO: Clean up code.

/**
 * Fails the aggregate test launcher before test discovery if it is not pinned
 * to the dedicated in-memory H2 database.
 *
 * <p>This check intentionally validates both JVM properties and environment
 * variables. Crypto Trader application defaults read {@code PSQL_*}, while
 * Spring Boot can independently read {@code SPRING_DATASOURCE_*}.</p>
 */
public final class GlobalTestDatabaseSafetyGuard implements LauncherSessionListener {

    private static final String SAFE_URL =
            "jdbc:h2:mem:crypto_trader_global_tests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    @Override
    public void launcherSessionOpened(LauncherSession session) {
        requireExactProperty("cryptotrader.global-tests.safe", "true");
        requireTestProfile();
        requireExactProperty("spring.datasource.url", SAFE_URL);
        requireExactProperty("spring.datasource.username", "sa");
        requireExactProperty("spring.datasource.driver-class-name", "org.h2.Driver");
        requireExactProperty("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
        requireExactProperty("spring.jpa.hibernate.ddl-auto", "create-drop");

        requireExactEnvironment("PSQL_DB_URL", SAFE_URL);
        requireExactEnvironment("PSQL_HOST", "localhost");
        requireExactEnvironment("PSQL_USER", "sa");
        requireExactEnvironment("SPRING_DATASOURCE_URL", SAFE_URL);
        requireExactEnvironment("SPRING_DATASOURCE_USERNAME", "sa");
        requireExactEnvironment("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "org.h2.Driver");

        rejectAnyPostgreSqlDatasourceSetting();
    }

    private static void requireTestProfile() {
        String profiles = System.getProperty("spring.profiles.active", "");
        boolean testIsActive = profiles.lines()
                .flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim)
                .anyMatch("test"::equals);

        if (!testIsActive) {
            fail("spring.profiles.active must include test", profiles);
        }
    }

    private static void requireExactProperty(String name, String expected) {
        String actual = System.getProperty(name);
        if (!expected.equals(actual)) {
            fail("JVM property " + name + " must equal " + expected, actual);
        }
    }

    private static void requireExactEnvironment(String name, String expected) {
        String actual = System.getenv(name);
        if (!expected.equals(actual)) {
            fail("environment variable " + name + " must equal " + expected, actual);
        }
    }

    private static void rejectAnyPostgreSqlDatasourceSetting() {
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            rejectPostgreSqlValue("JVM property " + entry.getKey(), String.valueOf(entry.getValue()));
        }
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            if (name.contains("datasource") || name.startsWith("psql_") || name.contains("database_url")) {
                rejectPostgreSqlValue("environment variable " + entry.getKey(), entry.getValue());
            }
        }
    }

    private static void rejectPostgreSqlValue(String source, String value) {
        if (value != null && value.toLowerCase(Locale.ROOT).contains("jdbc:postgresql:")) {
            fail(source + " contains a forbidden PostgreSQL URL", value);
        }
    }

    private static void fail(String requirement, String actual) {
        throw new IllegalStateException(
                "GLOBAL TEST DATABASE SAFETY CHECK FAILED: " + requirement
                        + ". Actual value: " + String.valueOf(actual)
                        + ". No tests were launched."
        );
    }
}

