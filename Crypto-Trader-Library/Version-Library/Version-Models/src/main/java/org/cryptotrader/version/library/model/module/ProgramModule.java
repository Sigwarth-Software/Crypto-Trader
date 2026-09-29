package org.cryptotrader.version.library.model.module;

import lombok.*;
import org.cryptotrader.version.library.model.config.ConfigFileType;

import java.nio.file.Path;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramModule {
    private ModuleLibrary moduleType;
    private Path modulePath;
    private ConfigFileType configFileType;
    private String name;
    private String version;

    public ProgramModule(final ModuleLibrary moduleType,
                         final Path modulePath,
                         final ConfigFileType configFileType) {
        this.moduleType = moduleType;
        this.modulePath = modulePath;
        this.configFileType = configFileType;
    }
}
