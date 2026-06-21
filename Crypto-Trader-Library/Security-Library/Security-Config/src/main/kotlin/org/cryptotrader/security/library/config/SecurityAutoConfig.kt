package org.cryptotrader.security.library.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.cryptotrader.security.library.entity.key.EncryptedKey
import org.cryptotrader.security.library.model.key.KeyEncrypter
import org.cryptotrader.security.library.infrastructure.IpBanFilter
import org.cryptotrader.security.library.entity.keyset.TinkKeyset
import org.cryptotrader.security.library.repository.keyset.TinkKeysetRepository
import org.cryptotrader.security.library.service.EncryptionService
import org.cryptotrader.security.library.service.InMemoryIpBanService
import org.cryptotrader.security.library.service.model.TinkKeysetStore
import org.cryptotrader.security.library.service.model.IpBanManager
import org.cryptotrader.security.library.service.IpPermaBanService
import org.cryptotrader.security.library.service.IpBanService
import org.cryptotrader.security.library.service.SecurityThreatService
import org.cryptotrader.security.library.service.entity.BannedIpAddressEntityService
import org.cryptotrader.security.library.service.entity.TinkKeysetEntityService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.messaging.converter.MessageConverter
import org.cryptotrader.security.library.event.SecurityEventLogger
import org.cryptotrader.security.library.model.BanType
import org.springframework.beans.factory.InitializingBean
import java.util.Base64
import javax.sql.DataSource

@AutoConfiguration(after = [DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class])
@EnableConfigurationProperties(SecurityPropertiesConfig::class)
@PropertySource(
    value = ["classpath:application-secure.yml"],
    factory = YamlPropertySourceFactory::class
)
open class SecurityAutoConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(TinkKeysetRepository::class, TinkKeyset::class)
    @ConditionalOnBean(DataSource::class)
    @EnableJpaRepositories(basePackageClasses = [TinkKeysetRepository::class])
    @EntityScan(basePackageClasses = [TinkKeyset::class])
    open class SecurityKeysetJpaConfig {

        @Bean
        @ConditionalOnMissingBean(TinkKeysetStore::class)
        open fun tinkKeysetStore(repository: TinkKeysetRepository): TinkKeysetStore {
            return TinkKeysetEntityService(repository)
        }
    }

    @Bean
    @ConditionalOnMissingBean
    open fun ipBanService(
        properties: SecurityPropertiesConfig,
        serviceProvider: ObjectProvider<BannedIpAddressEntityService>
    ): IpBanManager {
        val tempBanService = InMemoryIpBanService()
        val permaBanService: IpPermaBanService? = serviceProvider.getIfAvailable()?.let { IpPermaBanService(it) }
        // If we can't create a bean, we use the more sturdy in-memory ban
        // service.
        if (permaBanService == null) {
            properties.bans.denylist.forEach { ipOrCidr ->
                tempBanService.ban(ipOrCidr, BanType.PERMA)
            }
            return tempBanService
        }
        // If we can create a bean, we check both services.
        val banService = IpBanService(tempBanService, permaBanService)
        properties.bans.denylist.forEach { ipOrCidr ->
            banService.ban(ipOrCidr, BanType.PERMA)
        }
        return banService
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TinkKeysetStore::class)
    open fun encryptionService(
        properties: SecurityPropertiesConfig,
        keysetStore: TinkKeysetStore
    ): EncryptionService {
        val tink = properties.encryption?.tink ?: properties.crypto.tink
        return EncryptionService(
            keysetName = tink.keysetName,
            keysetStore = keysetStore,
            generateIfMissing = tink.generateIfMissing
        )
    }

    @Bean
    @ConditionalOnMissingBean(name = ["eventMessageEncryptionConverter"])
    @ConditionalOnBean(EncryptionService::class)
    open fun eventMessageEncryptionConverter(
        objectMapper: ObjectMapper,
        encryptionService: EncryptionService
    ): MessageConverter {
        return EventMessageEncryptionConverter(objectMapper, encryptionService)
    }

    @Bean
    @ConditionalOnBean(BannedIpAddressEntityService::class)
    open fun securityEventLogger(entityService: BannedIpAddressEntityService): SecurityEventLogger {
        return SecurityEventLogger(entityService)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(IpBanService::class)
    open fun handleSecurityThreat(
        ipBanService: IpBanService,
        properties: SecurityPropertiesConfig
    ): SecurityThreatService = SecurityThreatService(ipBanService, properties.http.blockResponseCode)

    @Bean
    @ConditionalOnBean(IpBanFilter::class)
    open fun ipBanFilterRegistration(
        filter: IpBanFilter,
        properties: SecurityPropertiesConfig
    ): FilterRegistrationBean<IpBanFilter> {
        val registration = FilterRegistrationBean(filter)
        registration.order = -100
        registration.setName("securityIpBanFilter")
        registration.isEnabled = properties.bans.enabled
        return registration
    }

    @Bean
    @ConditionalOnBean(EncryptionService::class)
    open fun configureEntityEncryption(encryptionService: EncryptionService): InitializingBean {
        return InitializingBean {
            EncryptedKey.setEncrypterDelegate(object : KeyEncrypter {
                override fun encrypt(key: String): String {
                    val encryptedBytes: ByteArray = encryptionService.encrypt(key.toByteArray(Charsets.UTF_8))
                    return Base64.getEncoder().encodeToString(encryptedBytes)
                }

                override fun decrypt(key: String): String {
                    val decodedBytes: ByteArray = Base64.getDecoder().decode(key)
                    val decryptedBytes: ByteArray = encryptionService.decrypt(decodedBytes)
                    return String(decryptedBytes, Charsets.UTF_8)
                }
            })
        }
    }
}
