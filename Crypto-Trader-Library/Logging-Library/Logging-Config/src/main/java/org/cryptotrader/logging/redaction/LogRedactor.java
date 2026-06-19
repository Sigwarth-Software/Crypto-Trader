package org.cryptotrader.logging.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cryptotrader.logging.properties.LogRedactionProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogRedactor {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LogRedactionProperties properties;

    public LogRedactor(LogRedactionProperties properties) {
        this.properties = properties;
    }

    public String redactHeader(@Nullable String name, @Nullable String value) {
        if (!this.properties.isEnabled() || value == null) {
            return value;
        }
        return this.isSensitiveHeader(name) ? this.properties.getReplacement() : value;
    }

    public String redactQueryString(@Nullable String queryString) {
        if (!this.properties.isEnabled() || !StringUtils.hasText(queryString)) {
            return queryString;
        }

        String[] pairs = queryString.split("&", -1);
        for (int index = 0; index < pairs.length; index++) {
            int separatorIndex = pairs[index].indexOf('=');
            if (separatorIndex < 0) {
                continue;
            }
            String encodedName = pairs[index].substring(0, separatorIndex);
            String name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
            if (this.isSensitiveField(name)) {
                pairs[index] = encodedName + "=" + URLEncoder.encode(this.properties.getReplacement(), StandardCharsets.UTF_8);
            }
        }
        return String.join("&", pairs);
    }

    public String redactText(@Nullable String text) {
        if (!this.properties.isEnabled() || !StringUtils.hasText(text)) {
            return text;
        }

        String redacted = text;
        for (String field : this.properties.getFields()) {
            redacted = this.redactAssignment(redacted, field);
        }
        return redacted;
    }

    public JsonNode redact(JsonNode node) {
        if (!this.properties.isEnabled() || node == null) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode copy = (ObjectNode) node.deepCopy();
            Iterator<Map.Entry<String, JsonNode>> fields = copy.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (this.isSensitiveField(entry.getKey())) {
                    copy.put(entry.getKey(), this.properties.getReplacement());
                } else {
                    copy.set(entry.getKey(), this.redact(entry.getValue()));
                }
            }
            return copy;
        }

        if (node.isArray()) {
            ArrayNode copy = OBJECT_MAPPER.createArrayNode();
            node.forEach(child -> copy.add(this.redact(child)));
            return copy;
        }

        return node;
    }

    private String redactAssignment(String text, String field) {
        Pattern pattern = Pattern.compile(
                "(?i)([?&\\s,{\\[]?)(\"?" + Pattern.quote(field) + "\"?\\s*[:=]\\s*)(\"?)([^\"&\\s,}\\]]+)(\"?)"
        );
        Matcher matcher = pattern.matcher(text);
        StringBuilder redacted = new StringBuilder();
        while (matcher.find()) {
            String replacement = matcher.group(1)
                    + matcher.group(2)
                    + matcher.group(3)
                    + this.properties.getReplacement()
                    + matcher.group(5);
            matcher.appendReplacement(redacted, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(redacted);
        return redacted.toString();
    }

    private boolean isSensitiveHeader(@Nullable String header) {
        if (header == null) {
            return false;
        }
        return this.properties.getHeaders().stream()
                .anyMatch(sensitiveHeader -> sensitiveHeader.equalsIgnoreCase(header));
    }

    private boolean isSensitiveField(@Nullable String field) {
        if (field == null) {
            return false;
        }
        return this.properties.getFields().stream()
                .anyMatch(sensitiveField -> sensitiveField.equalsIgnoreCase(field));
    }
}
