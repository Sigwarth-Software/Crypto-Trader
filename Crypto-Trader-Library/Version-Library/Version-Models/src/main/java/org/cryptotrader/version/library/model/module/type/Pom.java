package org.cryptotrader.version.library.model.module.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.cryptotrader.version.library.model.config.ConfigFileType;
import org.cryptotrader.version.library.model.dependency.type.PomDependency;
import org.cryptotrader.version.library.model.module.ModuleLibrary;
import org.cryptotrader.version.library.model.module.ProgramModule;
import org.cryptotrader.version.library.model.module.ProgramModuleDescendent;

import java.nio.file.Path;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class Pom extends ProgramModuleDescendent {
    private PomDependency moduleDependency;
    public Pom(final ModuleLibrary moduleType,
               final Path modulePath,
               final ConfigFileType configFileType,
               final PomDependency moduleDependency) {
        super(moduleType, modulePath, configFileType);
        this.moduleDependency = moduleDependency;
    }
    public Pom(final ModuleLibrary moduleType,
               final Path modulePath,
               final ConfigFileType configFileType,
               final PomDependency moduleDependency,
               final ProgramModule parent,
               final ProgramModule child) {
        super(moduleType, modulePath, configFileType, parent, child);
        this.moduleDependency = moduleDependency;
    }
}
