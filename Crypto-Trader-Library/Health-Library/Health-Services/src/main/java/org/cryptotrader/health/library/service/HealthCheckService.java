package org.cryptotrader.health.library.service;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.health.library.entity.HealthStatus;
import org.cryptotrader.health.library.model.CryptoTraderService;
import org.cryptotrader.health.library.model.ServiceStatus;
import org.cryptotrader.health.library.service.entity.HealthStatusEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class HealthCheckService {

    private final RestTemplate restTemplate;

    private final HealthStatusEntityService healthStatusEntityService;

    @Value("${PSQL_HOST:localhost}")
    private String psqlHost;

    @Value("${CT_DATA_HOST:localhost}")
    private String ctDataHost;


    @Autowired
    public HealthCheckService(final RestTemplate restTemplate,
                              final HealthStatusEntityService healthStatusEntityService) {
        this.restTemplate = restTemplate;
        this.healthStatusEntityService = healthStatusEntityService;
    }

    public void checkAndPersist(final CryptoTraderService service) {
        if (service == CryptoTraderService.DATABASE) {
            this.checkDatabase();
            return;
        }

        final String url = this.getHealthUrl(service);
        ServiceStatus status;
        int httpCode = 0;
        String details = null;

        try {
            final ResponseEntity<String> response = this.restTemplate.getForEntity(url, String.class);
            httpCode = response.getStatusCode().value();
            status = (httpCode == 200) ? ServiceStatus.ALIVE : ServiceStatus.DEAD;
            details = (httpCode == 200) ? "HTTP 200 OK" : response.getBody();
        } catch (final Exception e) {
            status = ServiceStatus.DEAD;
            details = e.getMessage();
            log.warn("Health check failed for {}: {}", service, e.getMessage());
        }

        final HealthStatus entity = HealthStatus.builder()
            .service(service)
            .status(status)
            .httpStatusCode(httpCode)
            .details(details)
            .checkedAt(LocalDateTime.now(ZoneId.of("America/Chicago")))
            .build();

        this.healthStatusEntityService.save(entity);
        log.info("Health check for {}: {} (HTTP {})", service, status, httpCode);
    }

    private void checkDatabase() {
        ServiceStatus status;
        String details = null;

        try (final Socket socket = new Socket(this.psqlHost, CryptoTraderService.DATABASE.getPort())) {
            status = ServiceStatus.ALIVE;
            details = "PostgreSQL accepting connections at " + this.psqlHost + ":" + CryptoTraderService.DATABASE.getPort();
        } catch (final Exception e) {
            status = ServiceStatus.DEAD;
            details = e.getMessage();
            log.warn("Health check failed for {}: {}", CryptoTraderService.DATABASE, e.getMessage());
        }

        final HealthStatus entity = HealthStatus.builder()
            .service(CryptoTraderService.DATABASE)
            .status(status)
            .httpStatusCode(0)
            .details(details)
            .checkedAt(LocalDateTime.now(ZoneId.of("America/Chicago")))
            .build();

        this.healthStatusEntityService.save(entity);
        log.info("Health check for {}: {}", CryptoTraderService.DATABASE, status);
    }

    private String getHealthUrl(final CryptoTraderService service) {
        if (service == CryptoTraderService.DOCS) {
            // TODO: Replace hardcoded URL.
            return "https://sigwarth-software.github.io/Crypto-Trader/";
        }
        if (service == CryptoTraderService.DATA) {
            return "http://" + this.ctDataHost + ":" + service.getPort() + "/actuator/health";
        }
        return "http://localhost:" + service.getPort() + "/actuator/health";
    }
}
