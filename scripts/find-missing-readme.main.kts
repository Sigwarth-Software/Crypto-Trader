@file:DependsOn("org.jetbrains:annotations:26.0.1")
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


val buildFiles: Set<String> = setOf("pom.xml", "build.gradle.kts", "build.gradle")


fun main(args: Array<String>) {
    val projectRoot: Path = if (args.isNotEmpty()) {
        Paths.get(args[0]).toAbsolutePath().normalize()
    } else {
        Paths.get("").toAbsolutePath().normalize()
    }

    if (!Files.exists(projectRoot) || !Files.isDirectory(projectRoot)) {
        println("Error: Project root $projectRoot does not exist or is not a directory.")
        return
    }

    println("Scanning for modules missing README.md in: $projectRoot\n")

    val missing: MutableList<String> = mutableListOf()

    Files.walk(projectRoot)
        .filter {
            Files.isDirectory(it)
        }
        .filter { dir ->
            val fileName: String = dir.fileName.toString()
            isIgnoredPath(fileName)
        }
        .filter { dir ->
            buildFiles.any { Files.exists(dir.resolve(it)) }
        }
        .forEach { dir ->
            if (!Files.exists(dir.resolve("README.md"))) {
                val relativePath: String = projectRoot.relativize(dir).toString()
                missing.add(relativePath)
            }
        }

    if (missing.isEmpty()) {
        println("All modules have a README.md file.")
    } else {
        println("Modules missing README.md (${missing.size}):")
        missing.sorted().forEach { println("  - $it") }
    }
}

fun isIgnoredPath(name: String): Boolean {
    return !name.startsWith(".") &&
        name != "target" &&
        name != "build" &&
        name != "node_modules"
}

main(args)
