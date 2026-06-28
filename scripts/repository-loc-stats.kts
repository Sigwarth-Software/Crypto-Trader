import java.io.File

/**
 * Script to count the lines of code (LOC) in the Crypto-Trader source code.
 * Excludes generated files, documentation, and dependencies.
 */
fun main() {
    val projectRoot = File(".")
    var totalLines = 0L
    val languageLines: MutableMap<String, Long> = mutableMapOf()
    val languageFileCount: MutableMap<String, Int> = mutableMapOf()
    val moduleLines: MutableMap<String, Long> = mutableMapOf()
    val moduleFileCount: MutableMap<String, Int> = mutableMapOf()
    val libraryLines: MutableMap<String, Long> = mutableMapOf()
    val libraryFileCount: MutableMap<String, Int> = mutableMapOf()

    val includedExtensions: Set<String> = setOf("java", "kt", "kts", "ts", "py", "html", "scss", "xml", "yml", "yaml", "sql", "fxml")
    val excludedDirs: Set<String> = setOf(
        "target", "node_modules", "build", ".angular", ".idea", ".git",
        "Crypto-Trader-Docs", "docs", "site", "playwright-report",
        "dist", "out", "coverage", "bin", "obj", "tink", "logs", "infra",
        "certs"
    )
    var numTotalFiles = 0
    println("Counting lines of code in ${projectRoot.absolutePath}...")

    projectRoot.walkTopDown()
        .onEnter { dir ->
            // Skip excluded directories early
            val name: String = dir.name.lowercase()
            !excludedDirs.any { it.equals(name, ignoreCase = true) }
        }
        .filter { it.isFile && includedExtensions.contains(it.extension) }
        .filter { file ->
            // Additional check for generated Angular JS files or other common generated patterns
            !file.name.endsWith(".component.js") &&
            !file.name.endsWith(".service.js") &&
            !file.name.endsWith(".map")
        }
        .forEach { file ->
            try {
                numTotalFiles++
                val lines: Long = file.readLines().size.toLong()
                totalLines += lines

                // Language stats
                val extension: String = file.extension.lowercase()
                languageLines[extension] = languageLines.getOrDefault(extension, 0L) + lines
                languageFileCount[extension] = languageFileCount.getOrDefault(extension, 0) + 1
                // Module stats
                val relativePath: String = file.relativeTo(projectRoot).path
                val pathParts: List<String> = relativePath.split(File.separator)
                val moduleName: String = if (pathParts.size > 1) {
                    val topDir: String = pathParts[0]
                    if (topDir.startsWith("Crypto-Trader-")) topDir else "Other"
                } else {
                    "Root"
                }
                moduleLines[moduleName] = moduleLines.getOrDefault(moduleName, 0L) + lines
                moduleFileCount[moduleName] = moduleFileCount.getOrDefault(moduleName, 0) + 1
                // Library stats
                if (moduleName == "Crypto-Trader-Library" && pathParts.size > 2) {
                    val libDir = pathParts[1]
                    if (libDir.endsWith("-Library")) {
                        libraryLines[libDir] = libraryLines.getOrDefault(libDir, 0L) + lines
                        libraryFileCount[libDir] = libraryFileCount.getOrDefault(libDir, 0) + 1
                    }
                }
            } catch (exception: Exception) {
                System.err.println("Error reading file ${file.path}: ${exception.message}")
            }
        }

    println("\n--- Source Lines of Code Report ---")
    println("Total Source Lines of Code: $totalLines, across $numTotalFiles files")
    println("------------------------------------")
    languageLines.entries
        .sortedByDescending { it.value }
        .forEach { (ext, count) ->
            val percentage = if (totalLines > 0) (count.toDouble() / totalLines * 100) else 0.0
            println("${ext.uppercase().padEnd(5)}: ${count.toString().padStart(8)} lines (${String.format("%.2f", percentage)}%), ${languageFileCount[ext]} files")
        }

    println("\n--- Module Breakdown ---")
    moduleLines.entries
        .sortedByDescending { it.value }
        .forEach { (module, count) ->
            val percentage = if (totalLines > 0) (count.toDouble() / totalLines * 100) else 0.0
            println("${module.padEnd(25)}: ${count.toString().padStart(8)} lines (${String.format("%.2f", percentage)}%), ${moduleFileCount[module]} files")
        }

    if (libraryLines.isNotEmpty()) {
        println("\n--- Library Breakdown (within Crypto-Trader-Library) ---")
        val totalLibLines = libraryLines.values.sum()
        libraryLines.entries
            .sortedByDescending { it.value }
            .forEach { (lib, count) ->
                val percentage = if (totalLines > 0) (count.toDouble() / totalLines * 100) else 0.0
                val libPercentage = if (totalLibLines > 0) (count.toDouble() / totalLibLines * 100) else 0.0
                println("${lib.padEnd(25)}: ${count.toString().padStart(8)} lines (${String.format("%.2f", percentage)}% of total, ${String.format("%.2f", libPercentage)}% of libs), ${libraryFileCount[lib]} files")
            }
    }
    println("------------------------------------")
}

main()
