import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

/**
 * Script to remove files from logs directories while keeping the directories.
 *
 * Usage:
 *   kotlin scripts/clean-logs.kts
 *   kotlin scripts/clean-logs.kts -- <project-root>
 *   kotlin scripts/clean-logs.kts -- --dry-run
 *   kotlin scripts/clean-logs.kts -- <project-root> --dry-run
 */

fun main(args: Array<String>) {
    val dryRun = args.any { it == "--dry-run" }
    val projectRootArg = args.firstOrNull { it != "--dry-run" }
    val projectRoot = resolveProjectRoot(projectRootArg)

    if (!Files.exists(projectRoot) || !projectRoot.isDirectory()) {
        println("Error: Project root $projectRoot does not exist or is not a directory.")
        return
    }

    var deletedFiles = 0L
    var deletedBytes = 0L
    var failedFiles = 0L
    val excludedDirs = setOf(".git", ".idea", ".angular", "node_modules", "Crypto-Trader-Docs", "docs", "site")

    println("${if (dryRun) "Finding" else "Removing"} log files in $projectRoot...")

    Files.walk(projectRoot).use { paths ->
        paths.asSequence()
            .filter { it.isRegularFile() }
            .filter { !isInExcludedDirectory(projectRoot, it, excludedDirs) }
            .filter { isInLogsDirectory(projectRoot, it) }
            .forEach { file ->
                try {
                    val bytes = Files.size(file)
                    if (!dryRun) {
                        Files.delete(file)
                    }
                    deletedFiles++
                    deletedBytes += bytes
                } catch (e: Exception) {
                    failedFiles++
                    System.err.println("Error deleting file ${file.toAbsolutePath()}: ${e.message}")
                }
            }
    }

    println("\n--- Clean Logs Report ---")
    println("Project Root : $projectRoot")
    println("Mode         : ${if (dryRun) "dry run" else "delete"}")
    println("Files ${if (dryRun) "found" else "deleted"}: ${deletedFiles.toString().padStart(12)}")
    println("Space ${if (dryRun) "found" else "deleted"}: ${formatBytes(deletedBytes)} (${deletedBytes.toString().padStart(12)} bytes)")
    println("Failures     : ${failedFiles.toString().padStart(12)}")
    println("-------------------------")
}

fun resolveProjectRoot(projectRootArg: String?): Path {
    if (projectRootArg != null) {
        return Paths.get(projectRootArg).toAbsolutePath().normalize()
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
