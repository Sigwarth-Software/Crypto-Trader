package org.cryptotrader.security.library.entity.ip;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "banned_ips", indexes = {
        @Index(name = "ix_banned_ip_address", columnList = "ip_address")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BannedIpAddress extends IpAddress {

    @Builder.Default
    @Column(nullable = false, name = "occurred_at")
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false, name = "attempts")
    private int attempts = 1;

    public BannedIpAddress(String ipAddress) {
        super(ipAddress);
        this.occurredAt = LocalDateTime.now();
    }
}
