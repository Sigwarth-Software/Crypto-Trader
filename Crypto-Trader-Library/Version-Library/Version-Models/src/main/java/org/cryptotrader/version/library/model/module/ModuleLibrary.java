package org.cryptotrader.version.library.model.module;

import lombok.Getter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Getter
public class ModuleLibrary {
    // TODO: Remove hard-coded dependencies.
    public static String[] CRYPTO_TRADER_PARENT = {
            "Crypto-Trader-Admin",
            "Crypto-Trader-Agent",
//            "Crypto-Trader-Analysis", Not a Java/Kotlin dependency.
            "Crypto-Trader-Api",
            "Crypto-Trader-Assets",
            "Crypto-Trader-Chat",
            "Crypto-Trader-Console",
            "Crypto-Trader-Contact",
            "Crypto-Trader-Coverage",
            "Crypto-Trader-Data",
            "Crypto-Trader-Docs",
            "Crypto-Trader-Engine",
            "Crypto-Trader-Health",
            "Crypto-Trader-Library",
            "Crypto-Trader-Logging",
//            "Crypto-Trader-Mobile", Gradle dependency.
            "Crypto-Trader-Security",
            "Crypto-Trader-Simulator",
            "Crypto-Trader-Testing",
            "Crypto-Trader-Transactions",
            "Crypto-Trader-Version",
//            "Crypto-Trader-Website" Not a Java/Kotlin dependency.
    };

    public static String[] LIBRARY_PARENT = {
            "Admin-Library",
            "Admin-Events",
            "Admin-Models",
            "Agent-Library",
            "Agent-Communication",
            "Agent-Components",
            "Agent-Config",
            "Agent-Infrastructure",
            "Agent-Models",
            "Api-Library",
            "Api-Communication",
            "Api-Components",
            "Api-Config",
            "Api-Events",
            "Api-Extensions",
            "Api-Infrastructure",
            "Api-Models",
            "Api-Repositories",
            "Api-Scripts",
            "Api-Services",
            "Chat-Library",
            "Chat-Communication",
            "Chat-Components",
            "Chat-Config",
            "Chat-Events",
            "Chat-Models",
            "Chat-Repositories",
            "Chat-Services",
            "Console-Library",
            "Console-Communication",
            "Console-Components",
            "Console-Events",
            "Console-Models",
            "Console-Services",
            "Contact-Library",
            "Contact-Events",
            "Contact-Models",
            "Contact-Repositories",
            "Data-Library",
            "Data-Communication",
            "Data-Components",
            "Data-Models",
            "Data-Repositories",
            "Data-Services",
            "Desktop-Library",
            "Desktop-Components",
            "Desktop-Styles",
            "Engine-Library",
            "Engine-Services",
            "Externals-Library",
            "Health-Library",
            "Health-Communication",
            "Health-Components",
            "Health-Config",
            "Health-Models",
            "Health-Repositories",
            "Health-Services",
            "Logging-Library",
            "Logging-Communication",
            "Logging-Config",
            "Logging-Events",
            "Logging-Infrastructure",
            "Logging-Models",
            "Logging-Repositories",
            "Logging-Scripts",
            "Logging-Services",
            "Security-Library",
            "Security-Config",
            "Security-Events",
            "Security-Infrastructure",
            "Security-Models",
            "Security-Repositories",
            "Security-Services",
            "Testing-Library",
            "Testing-Infrastructure",
            "Testing-Scripts",
            "Universal-Library",
            "Universal-Communication",
            "Universal-Components",
            "Universal-Config",
            "Universal-Events",
            "Universal-Extensions",
            "Universal-Models",
            "Universal-Services",
            "Version-Library",
            "Version-Models"
    };

    public static List<ModuleLibrary> MODULES = getInitializedModules();

    private String name;

    public static List<ModuleLibrary> getInitializedModules() {
        List<ModuleLibrary> moduleLibraries = new ArrayList<>(Arrays.stream(CRYPTO_TRADER_PARENT)
                                                                    .map(ModuleLibrary::new)
                                                                    .toList());
        moduleLibraries.addAll(Arrays.stream(LIBRARY_PARENT)
                                     .map(ModuleLibrary::new)
                                     .toList());
        moduleLibraries.add(new ModuleLibrary("Crypto-Trader"));
        return moduleLibraries;
    }

    public static void initializeModules(List<ModuleLibrary> modules) {
        ModuleLibrary.MODULES = modules;
    }

    public ModuleLibrary(String name) {
        this.name = name;
    }

    public static ModuleLibrary resolveFromPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        if (MODULES == null || MODULES.isEmpty()) {
            throw new IllegalStateException("ModuleLibrary has not been initialized. Call ModuleLibrary.initializeModules(...) before resolving modules. Path=" + path);
        }
        String pathText = path.toString();
        String[] segments = pathText.split("[\\\\/]+");
        ModuleLibrary deepestSegmentMatch = null;
        int deepestIndex = -1;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            for (ModuleLibrary module : MODULES) {
                if (segment.equals(module.getName())) {
                    if (i > deepestIndex) {
                        deepestIndex = i;
                        deepestSegmentMatch = module;
                    }
                }
            }
        }
        if (deepestSegmentMatch != null) {
            return deepestSegmentMatch;
        }
        return MODULES.stream()
                .filter(module -> pathText.contains(module.getName()))
                .max(Comparator.comparingInt(module -> module.getName().length()))
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve module from path: " + path + ". Known modules=" + MODULES.stream().map(ModuleLibrary::getName).toList()));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object instanceof ModuleLibrary module) {
            return this.name.equals(module.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
