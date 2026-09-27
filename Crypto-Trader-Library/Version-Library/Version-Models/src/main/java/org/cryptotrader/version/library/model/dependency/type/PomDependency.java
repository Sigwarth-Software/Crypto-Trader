package org.cryptotrader.version.library.model.dependency.type;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.cryptotrader.version.library.model.dependency.Dependency;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PomDependency extends Dependency {
    private String groupId;
    private String artifactId;

    public PomDependency(final String name, final String version,
                         final String groupId, final String artifactId) {
        super(name, version);
        this.groupId = groupId;
        this.artifactId = artifactId;
    }
}
