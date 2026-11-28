import java.io.File

// TODO: Clean up code.

fun main() {
    val moduleInfoPath = "src/main/java/module-info.java"

    val projectRoot = File(".")
    val missingModules = mutableListOf<String>()

    projectRoot.walk()
        .filter { it.isDirectory && File(it, "pom.xml").exists() }
        .forEach { moduleDir ->
            if (shouldHaveModuleInfo(moduleDir)) {
                val moduleInfoFile = File(moduleDir, moduleInfoPath)
                if (!moduleInfoFile.exists()) {
                    val relativePath = moduleDir.relativeTo(projectRoot).path
                    val displayName = relativePath.ifEmpty { "root" }
                    missingModules.add(displayName)
                }
            }
        }

    if (missingModules.isEmpty()) {
        println("All modules have a module-info.java file.")
    } else {
        println("Found ${missingModules.size} modules missing module-info.java:")
        missingModules.sortedBy { it }.forEach { println(" - $it") }
    }
}

fun shouldHaveModuleInfo(moduleDir: File): Boolean {
    val srcMain = File(moduleDir, "src/main")
    if (!srcMain.exists()) return false

    val javaSrc = File(srcMain, "java")
    val kotlinSrc = File(srcMain, "kotlin")

    fun hasSourceFiles(dir: File): Boolean {
        if (!dir.exists()) return false
        return dir.walk().any { it.isFile && (it.name.endsWith(".java") || it.name.endsWith(".kt")) }
    }

    return hasSourceFiles(javaSrc) || hasSourceFiles(kotlinSrc)
}

main()
