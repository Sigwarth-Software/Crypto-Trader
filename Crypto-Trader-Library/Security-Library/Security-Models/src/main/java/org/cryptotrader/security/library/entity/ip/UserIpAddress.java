package org.cryptotrader.security.library.entity.ip;

import jakarta.persistence.*;
import lombok.*;
import org.cryptotrader.api.library.entity.user.ProductUser;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_ip_addresses", indexes = {
        @Index(name = "ix_user_ip_address_user", columnList = "user_id"),
        @Index(name = "ix_user_ip_address_ip", columnList = "ip_address")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIpAddress extends IpAddress {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private ProductUser user;

    @Builder.Default
    @Column(name = "last_accessed", nullable = false)
    private LocalDateTime lastAccessed = LocalDateTime.now();

    public UserIpAddress(final ProductUser user, final String ipAddress) {
        super(ipAddress);
        this.user = user;
        this.lastAccessed = LocalDateTime.now();
    }

    public UserIpAddress(final ProductUser user, final String ipAddress, final LocalDateTime lastAccessed) {
        super(ipAddress);
        this.user = user;
        this.lastAccessed = lastAccessed;
    }
}
