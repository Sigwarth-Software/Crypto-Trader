package org.cryptotrader.security.library.entity.ip;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cryptotrader.api.library.entity.user.ProductUser;

@Getter
@Setter
@Entity
@Table(name = "user_ip_addresses")
@NoArgsConstructor
@AllArgsConstructor
public class UserIpAddress extends IpAddress {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private ProductUser user;
}
