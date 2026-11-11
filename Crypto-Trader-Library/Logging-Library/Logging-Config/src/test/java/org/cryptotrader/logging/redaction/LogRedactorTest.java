package org.cryptotrader.logging.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptotrader.logging.properties.LogRedactionProperties;
import org.cryptotrader.testing.library.infrastructure.annotation.UnitSuite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogRedactor")
@UnitSuite
class LogRedactorTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LogRedactor redactor = new LogRedactor(new LogRedactionProperties());

    @Nested
    @DisplayName("redact")
    class Redact {
        @DisplayName("should mask nested sensitive JSON fields")
        @Test
        void maskNestedSensitiveJsonFields() throws Exception {
            final JsonNode input = OBJECT_MAPPER.readTree("""
                    {
                      "email": "user@example.com",
                      "password": "secret",
                      "profile": {
                        "accessToken": "abc123"
                      }
                    }
                    """);

            final JsonNode redacted = LogRedactorTest.this.redactor.redact(input);

            assertEquals("user@example.com", redacted.get("email").asText());
            assertEquals("[REDACTED]", redacted.get("password").asText());
            assertEquals("[REDACTED]", redacted.get("profile").get("accessToken").asText());
        }
    }

    @Nested
    @DisplayName("redactHeader")
    class RedactHeader {
        @Nested
        @DisplayName("when the header is sensitive")
        class WhenHeaderIsSensitive {
            @Test
            @DisplayName("should mask the value case insensitively")
            void maskValueCaseInsensitively() {
                assertEquals("[REDACTED]", redactor.redactHeader("Authorization", "Bearer token"));
            }
        }

        @Nested
        @DisplayName("when the header is not sensitive")
        class WhenHeaderIsNotSensitive {
            @Test
            @DisplayName("should preserve the value")
            void preserveValue() {
                assertEquals("application/json", redactor.redactHeader("Content-Type", "application/json"));
            }
        }
    }

    @Nested
    @DisplayName("redactQueryString")
    class RedactQueryString {
        @DisplayName("should mask configured field values")
        @Test
        void maskConfiguredFieldValues() {
            final String redacted = LogRedactorTest.this.redactor.redactQueryString("email=user%40example.com&password=secret");

            assertTrue(redacted.contains("email=user%40example.com"));
            assertFalse(redacted.contains("secret"));
            assertTrue(redacted.contains("password=%5BREDACTED%5D"));
        }
    }

    @Nested
    @DisplayName("redactText")
    class RedactText {
        @DisplayName("should mask form style sensitive fields")
        @Test
        void maskFormStyleSensitiveFields() {
            final String redacted = LogRedactorTest.this.redactor.redactText("email=user@example.com&password=secret&token=abc123");

            assertTrue(redacted.contains("email=user@example.com"));
            assertFalse(redacted.contains("secret"));
            assertFalse(redacted.contains("abc123"));
            assertTrue(redacted.contains("password=[REDACTED]"));
            assertTrue(redacted.contains("token=[REDACTED]"));
        }
    }
}
