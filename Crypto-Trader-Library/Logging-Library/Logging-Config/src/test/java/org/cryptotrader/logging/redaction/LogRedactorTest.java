package org.cryptotrader.logging.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptotrader.logging.properties.LogRedactionProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogRedactorTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LogRedactor redactor = new LogRedactor(new LogRedactionProperties());

    @Test
    void redact_MasksNestedSensitiveJsonFields() throws Exception {
        JsonNode input = OBJECT_MAPPER.readTree("""
                {
                  "email": "user@example.com",
                  "password": "secret",
                  "profile": {
                    "accessToken": "abc123"
                  }
                }
                """);

        JsonNode redacted = this.redactor.redact(input);

        assertEquals("user@example.com", redacted.get("email").asText());
        assertEquals("[REDACTED]", redacted.get("password").asText());
        assertEquals("[REDACTED]", redacted.get("profile").get("accessToken").asText());
    }

    @Test
    void redactHeader_MasksConfiguredHeaderNamesCaseInsensitively() {
        assertEquals("[REDACTED]", this.redactor.redactHeader("Authorization", "Bearer token"));
        assertEquals("application/json", this.redactor.redactHeader("Content-Type", "application/json"));
    }

    @Test
    void redactQueryString_MasksConfiguredFieldValues() {
        String redacted = this.redactor.redactQueryString("email=user%40example.com&password=secret");

        assertTrue(redacted.contains("email=user%40example.com"));
        assertFalse(redacted.contains("secret"));
        assertTrue(redacted.contains("password=%5BREDACTED%5D"));
    }

    @Test
    void redactText_MasksFormStyleSensitiveFields() {
        String redacted = this.redactor.redactText("email=user@example.com&password=secret&token=abc123");

        assertTrue(redacted.contains("email=user@example.com"));
        assertFalse(redacted.contains("secret"));
        assertFalse(redacted.contains("abc123"));
        assertTrue(redacted.contains("password=[REDACTED]"));
        assertTrue(redacted.contains("token=[REDACTED]"));
    }
}
