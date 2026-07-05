import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

data class FileSpaceStats(
    var bytes: Long = 0L,
    var files: Long = 0L
)

fun main(args: Array<String>) {
    val projectRoot: Path = resolveProjectRoot(args)

    if (!Files.exists(projectRoot) || !projectRoot.isDirectory()) {
        println("Error: Project root $projectRoot does not exist or is not a directory.")
        return
    }

    var totalBytes = 0L
    var totalFiles = 0L
    val extensionStats = mutableMapOf<String, FileSpaceStats>()
    val moduleStats = mutableMapOf<String, FileSpaceStats>()
    val logDirectoryStats = mutableMapOf<String, FileSpaceStats>()
    val libraryStats = mutableMapOf<String, FileSpaceStats>()
    val excludedDirs = setOf(".git", ".idea", ".angular", "node_modules", "Crypto-Trader-Docs", "docs", "site")

    println("Counting log file space in $projectRoot...")

    Files.walk(projectRoot).use { paths ->
        paths.asSequence()
            .filter { it.isRegularFile() }
            .filter { !isInExcludedDirectory(projectRoot, it, excludedDirs) }
            .filter { isInLogsDirectory(projectRoot, it) }
            .forEach { file ->
                try {
                    val bytes = Files.size(file)
                    val relativePath = projectRoot.relativize(file)
                    val pathParts = relativePath.map { it.toString() }

                    totalBytes += bytes
                    totalFiles++

                    addStats(extensionStats, extensionName(file), bytes)
                    addStats(moduleStats, moduleName(pathParts), bytes)
                    addStats(logDirectoryStats, logDirectoryName(projectRoot, file), bytes)

                    val module = moduleName(pathParts)
                    if (module == "Crypto-Trader-Library" && pathParts.size > 2) {
                        val libDir = pathParts[1]
                        if (libDir.endsWith("-Library")) {
                            addStats(libraryStats, libDir, bytes)
                        }
                    }
                } catch (e: Exception) {
                    System.err.println("Error reading file ${file.toAbsolutePath()}: ${e.message}")
                }
            }
    }

    println("\n--- Log File Space Report ---")
    println("Total Log File Space: ${formatBytes(totalBytes)} (${totalBytes.toString().padStart(12)} bytes)")
    println("Total Log Files     : ${totalFiles.toString().padStart(12)}")
    println("------------------------------------")

    printBreakdown("Extension Breakdown", extensionStats, totalBytes)
    printBreakdown("Module Breakdown", moduleStats, totalBytes)
    printBreakdown("Logs Directory Breakdown", logDirectoryStats, totalBytes)

    if (libraryStats.isNotEmpty()) {
        println("\n--- Library Breakdown (within Crypto-Trader-Library) ---")
        val totalLibBytes = libraryStats.values.sumOf { it.bytes }
        libraryStats.entries
            .sortedByDescending { it.value.bytes }
            .forEach { (lib, stats) ->
                val percentage = percentage(stats.bytes, totalBytes)
                val libPercentage = percentage(stats.bytes, totalLibBytes)
                println("${lib.padEnd(45)}: ${formatBytes(stats.bytes).padStart(10)} ${stats.files.toString().padStart(8)} files (${String.format("%.2f", percentage)}% of total, ${String.format("%.2f", libPercentage)}% of libs)")
            }
    }

    println("------------------------------------")
}

fun resolveProjectRoot(args: Array<String>): Path {
    if (args.isNotEmpty()) {
        return Paths.get(args[0]).toAbsolutePath().normalize()
    }

    val currentDir = Paths.get("").toAbsolutePath().normalize()
    return if (currentDir.fileName?.toString() == "scripts") {
        currentDir.parent
    } else {
        currentDir
    }
}

fun isInLogsDirectory(projectRoot: Path, file: Path): Boolean {
    val relativePath = projectRoot.relativize(file)
    return relativePath.parent?.any { it.toString().equals("logs", ignoreCase = true) } == true
}

fun isInExcludedDirectory(projectRoot: Path, file: Path, excludedDirs: Set<String>): Boolean {
    val relativePath = projectRoot.relativize(file)
    return relativePath.parent?.any { part ->
        excludedDirs.any { it.equals(part.toString(), ignoreCase = true) }
    } == true
}

fun extensionName(file: Path): String {
    val name = file.fileName.toString()
    val extension = File(name).extension.lowercase()
    return extension.ifEmpty { "(none)" }
}

fun moduleName(pathParts: List<String>): String {
    if (pathParts.size <= 1) {
        return "Root"
    }

    val topDir = pathParts[0]
    return if (topDir.startsWith("Crypto-Trader-")) topDir else "Other"
}

fun logDirectoryName(projectRoot: Path, file: Path): String {
    val relativePath = projectRoot.relativize(file)
    val parts = relativePath.map { it.toString() }
    val logsIndex = parts.indexOfFirst { it.equals("logs", ignoreCase = true) }
    if (logsIndex < 0) {
        return "(unknown)"
    }

    return parts.take(logsIndex + 1).joinToString(File.separator).ifEmpty { "logs" }
}

fun addStats(stats: MutableMap<String, FileSpaceStats>, key: String, bytes: Long) {
    val entry = stats.getOrPut(key) { FileSpaceStats() }
    entry.bytes += bytes
    entry.files++
}

fun printBreakdown(title: String, stats: Map<String, FileSpaceStats>, totalBytes: Long) {
    println("\n--- $title ---")
    if (stats.isEmpty()) {
        println("No log files found.")
        return
    }

    stats.entries
        .sortedByDescending { it.value.bytes }
        .forEach { (name, stats) ->
            val percentage = percentage(stats.bytes, totalBytes)
            println("${name.padEnd(45)}: ${formatBytes(stats.bytes).padStart(10)} ${stats.files.toString().padStart(8)} files (${String.format("%.2f", percentage)}%)")
        }
}

fun percentage(value: Long, total: Long): Double {
    return if (total > 0) value.toDouble() / total * 100 else 0.0
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) {
        return "$bytes B"
    }

    val units = listOf("KiB", "MiB", "GiB", "TiB", "PiB")
    var value = bytes.toDouble()
    var unitIndex = -1

    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }

    return String.format("%.2f %s", value, units[unitIndex])
}

main(args)
