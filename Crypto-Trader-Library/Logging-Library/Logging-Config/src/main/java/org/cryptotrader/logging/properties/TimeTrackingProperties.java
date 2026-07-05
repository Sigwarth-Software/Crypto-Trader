package org.cryptotrader.logging.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cryptotrader.logging.time-tracking")
public class TimeTrackingProperties {
    private double exceedingFactor = 0.8;
    private double expectedFactor = 1.4;
    private double warningFactor = 1.8;
    private double alertFactor = 2.0;
}
