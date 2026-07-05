package org.cryptotrader.security.library.infrastructure.config;

import org.cryptotrader.security.library.infrastructure.IpBanFilter;
import org.cryptotrader.security.library.service.model.IpBanManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

@AutoConfiguration
@ConditionalOnClass(OncePerRequestFilter.class)
public class IpBanAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IpBanFilter ipBanFilter(IpBanManager ipBanService,
                                   @Value("${security.ip-ban.block-status:429}") int blockStatus) {
        return new IpBanFilter(ipBanService, blockStatus);
    }
}
