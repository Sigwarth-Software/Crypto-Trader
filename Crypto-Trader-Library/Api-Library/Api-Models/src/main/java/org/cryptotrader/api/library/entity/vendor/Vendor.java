package org.cryptotrader.api.library.entity.vendor;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
public class Vendor {
    private String name;
    private double rate;
    public Vendor(final String name, final double rate) {
        this.name = name;
        this.rate = rate;
    }

    public double getAdjustedPrice(final double price) {
        return price + (price * this.rate);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object instanceof final @NotNull Vendor comparedVendor) {
            return this.name.equals(comparedVendor.getName());
        }
        return false;
    }
}
