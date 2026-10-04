package org.cryptotrader.logging.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cryptotrader.logging.properties.LogRedactionProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class that redacts sensitive information from log messages. */
public class LogRedactor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LogRedactionProperties properties;

    public LogRedactor(@NotNull final LogRedactionProperties properties) {
        this.properties = properties;
    }

    public @Nullable String redactHeader(@Nullable final String name,
                                         @Nullable final String value) {
        if (!this.properties.isEnabled() || value == null) {
            return value;
        }

        if (this.isSensitiveHeader(name)) {
            return this.properties.getReplacement();
        }
        return value;
    }

    public @Nullable String redactQueryString(@Nullable final String queryString) {
        if (!this.properties.isEnabled() || !StringUtils.hasText(queryString)) {
            return queryString;
        }

        final String[] pairs = queryString.split("&", -1);

        for (int index = 0; index < pairs.length; index++) {
            final int separatorIndex = pairs[index].indexOf('=');

            if (separatorIndex < 0) {
                continue;
            }
            final String encodedName = pairs[index].substring(0, separatorIndex);
            final String name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);

            if (this.isSensitiveField(name)) {
                pairs[index] = encodedName + "=" + URLEncoder.encode(
                    this.properties.getReplacement(),
                    StandardCharsets.UTF_8
                );
            }
        }
        return String.join("&", pairs);
    }

    public @Nullable String redactText(@Nullable final String text) {
        if (!this.properties.isEnabled() || !StringUtils.hasText(text)) {
            return text;
        }

        String redacted = text;
        for (final String field : this.properties.getFields()) {
            redacted = this.redactAssignment(redacted, field);
        }
        return redacted;
    }

    public @Nullable JsonNode redact(final @Nullable JsonNode node) {
        if (!this.properties.isEnabled() || node == null) {
            return node;
        }

        if (node.isObject()) {
            final ObjectNode copy = (ObjectNode) node.deepCopy();
            final Iterator<Map.Entry<String, JsonNode>> fields = copy.fields();
            while (fields.hasNext()) {
                final Map.Entry<String, JsonNode> entry = fields.next();
                if (this.isSensitiveField(entry.getKey())) {
                    copy.put(entry.getKey(), this.properties.getReplacement());
                } else {
                    copy.set(entry.getKey(), this.redact(entry.getValue()));
                }
            }
            return copy;
        }

        if (node.isArray()) {
            final ArrayNode copy = OBJECT_MAPPER.createArrayNode();
            node.forEach(child -> copy.add(this.redact(child)));
            return copy;
        }

        return node;
    }

    private @NotNull String redactAssignment(final @NotNull String text, final @NotNull String field) {
        final Pattern pattern = Pattern.compile(
                "(?i)([?&\\s,{\\[]?)(\"?" + Pattern.quote(field) + "\"?\\s*[:=]\\s*)(\"?)([^\"&\\s,}\\]]+)(\"?)"
        );
        final Matcher matcher = pattern.matcher(text);
        final StringBuilder redacted = new StringBuilder();
        while (matcher.find()) {
            final String replacement = matcher.group(1)
                    + matcher.group(2)
                    + matcher.group(3)
                    + this.properties.getReplacement()
                    + matcher.group(5);
            matcher.appendReplacement(redacted, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(redacted);
        return redacted.toString();
    }

    private boolean isSensitiveHeader(@Nullable final String header) {
        if (header == null) {
            return false;
        }
        return this.properties.getHeaders().stream()
                .anyMatch(sensitiveHeader -> sensitiveHeader.equalsIgnoreCase(header));
    }

    private boolean isSensitiveField(@Nullable final String field) {
        if (field == null) {
            return false;
        }
        return this.properties.getFields().stream()
                .anyMatch(sensitiveField -> sensitiveField.equalsIgnoreCase(field));
    }
}
