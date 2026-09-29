package org.cryptotrader.version.library.model.dependency;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
public class Dependency {
    private String name;
    private String version;

    public Dependency(final String name, final String version) {
        this.name = name;
        this.version = version;
    }
}
