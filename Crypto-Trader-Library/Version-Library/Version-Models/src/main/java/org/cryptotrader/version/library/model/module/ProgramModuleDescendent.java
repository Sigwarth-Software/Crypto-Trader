package org.cryptotrader.version.library.model.module;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cryptotrader.version.library.model.config.ConfigFileType;

import java.nio.file.Path;

@NoArgsConstructor
@Getter
public class ProgramModuleDescendent extends ProgramModule {
    private ProgramModule parent;
    private ProgramModule child;

    public ProgramModuleDescendent(final ModuleLibrary moduleType,
                                   final Path modulePath,
                                   final ConfigFileType configFileType) {
        super(moduleType, modulePath, configFileType);
        this.parent = null;
        this.child = null;
    }

    public ProgramModuleDescendent(final ModuleLibrary moduleType,
                                   final Path modulePath,
                                   final ConfigFileType configFileType,
                                   final ProgramModule parent,
                                   final ProgramModule child) {
        super(moduleType, modulePath, configFileType);
        this.parent = parent;
        this.child = child;
    }
}
