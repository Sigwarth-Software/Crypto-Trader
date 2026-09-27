package org.cryptotrader.api.library.entity.vendor;

import java.util.List;

public class SupportedVendors {
    public static final Vendor PAPER_MODE = new Vendor("Paper Mode", 0.0);
    public static final Vendor COINBASE = new Vendor("Coinbase", (0.5) / 100);
    public static final List<Vendor> VENDOR_LIST = List.of(PAPER_MODE, COINBASE);
    public static Vendor from(final String vendorName) {
        return VENDOR_LIST.stream().filter(vendor -> {
                    return vendor.getName().equalsIgnoreCase(vendorName);
                }).findFirst().orElse(PAPER_MODE);
    }
}
