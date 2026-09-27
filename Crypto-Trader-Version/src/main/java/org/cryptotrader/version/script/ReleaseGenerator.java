package org.cryptotrader.version.script;

import com.sigwarthsoftware.promo.github.commit.CommitRange;

import com.sigwarthsoftware.changelog.ReleaseChangelogGenerator;
import org.cryptotrader.version.library.model.module.ModuleLibrary;

import java.util.List;

public class ReleaseGenerator {
    public static List<com.sigwarthsoftware.changelog.version.models.module.ModuleLibrary> getAllSigwarthModules() {
        return ModuleLibrary.MODULES.stream().map(ReleaseGenerator::toSigwarthFormat).toList();
    }

    public static com.sigwarthsoftware.changelog.version.models.module.ModuleLibrary toSigwarthFormat(final ModuleLibrary library) {
        return new com.sigwarthsoftware.changelog.version.models.module.ModuleLibrary(library.getName());
    }

    public static void main(final String[] args) {
        com.sigwarthsoftware.changelog.version.models.module.ModuleLibrary.initializeModules(getAllSigwarthModules());
        final String releaseVersion = "0.3.0";
        final CommitRange commitRange = new CommitRange("fde9d343af0572f0b7149832e0d066ded7cd2ed8", "12c0e38530511920c6d318fcaf71df9254722e73");
        final String changelog = ReleaseChangelogGenerator.getChangelog(commitRange, releaseVersion);
    }
}
