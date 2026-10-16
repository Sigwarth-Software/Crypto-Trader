package org.cryptotrader.api.library.config;

import org.cryptotrader.chat.library.config.ChatWebClientConfig;
import org.cryptotrader.universal.library.config.HttpClientConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
    DefaultPropertiesConfig.class,
    ApplicationYamlConfig.class,
    ApplicationDocsYamlConfig.class,
    ApplicationTestYamlConfig.class,
    CorsConfig.class,
    HttpClientConfig.class,
    DocsSecurityConfig.class,
    ChatWebClientConfig.class
})
public class ApiConfigAutoConfiguration {
}
