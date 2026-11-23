import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.time.OffsetDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private fun git(vararg args: String): String {
    val process = ProcessBuilder("git", *args).directory(File(".")).start()
    val out = process.inputStream.bufferedReader().readText().trim()
    val err = process.errorStream.bufferedReader().readText().trim()
    if (process.waitFor() != 0) throw GradleException("git ${args.joinToString(" ")} failed: $err")
    return out
}

data class AppVersion(val year: Int, val month: Int, val code: Int) {
    override fun toString() = "$year.$month.$code"

    companion object {
        private val buildFile = File("app/build.gradle.kts")
        private val codeRegex = Regex("versionCode = (\\d+)")
        private val nameRegex = Regex("versionName = \"([^\"]+)\"")

        fun read(): AppVersion {
            val text = buildFile.readText()
            val code = codeRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()
                ?: throw GradleException("Unable to parse version code")
            val name = nameRegex.find(text)?.groupValues?.get(1)
                ?: throw GradleException("Unable to parse version name")
            val parts = name.split(".")
            if (parts.size != 3) throw GradleException("Unsupported version name $name")
            val version = AppVersion(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            if (version.code.toString() != code.toString()) {
                throw GradleException("Mismatching version code and version name")
            }
            return version
        }

        fun write(version: AppVersion) {
            val text = buildFile.readText()
                .replace(codeRegex, "versionCode = ${version.code}")
                .replace(nameRegex, "versionName = \"$version\"")
            buildFile.writeText(text)
        }
    }
}

abstract class VersionPrintTask : DefaultTask() {
    @TaskAction
    fun run() = println(AppVersion.read().toString())
}

abstract class VersionBumpTask : DefaultTask() {
    @TaskAction
    fun run() {
        val current = AppVersion.read()
        val now = OffsetDateTime.now()
        val next = AppVersion(now.year, now.monthValue, current.code + 1)
        AppVersion.write(next)
        println("Version bumped from $current to $next!")
    }
}

abstract class VersionPrereleaseTask : DefaultTask() {
    @TaskAction
    fun run() {
        val current = AppVersion.read()
        val sha = git("rev-parse", "--short", "HEAD")
        val committed = git("show", "--no-patch", "--no-notes", "--pretty=%cI", sha)
        val time = OffsetDateTime.parse(committed.trim('\''))
        println("${time.year}.${time.monthValue}.${current.code + 1}-nightly+$sha")
    }
}

abstract class TagExistsTask : DefaultTask() {
    @TaskAction
    fun run() {
        val tag = project.findProperty("tag") as String?
            ?: throw GradleException("Missing property: tag")
        val process = ProcessBuilder("git", "ls-remote", "--exit-code", "--tags", "origin", tag).start()
        val exists = process.waitFor() == 0
        if (exists) throw GradleException("Tag $tag already exists")
        println("Tag $tag is available")
    }
}

abstract class TagExistsYesNoTask : DefaultTask() {
    @TaskAction
    fun run() {
        val tag = project.findProperty("tag") as String?
            ?: throw GradleException("Missing property: tag")
        val process = ProcessBuilder("git", "ls-remote", "--exit-code", "--tags", "origin", tag).start()
        println(if (process.waitFor() == 0) "yes" else "no")
    }
}

abstract class LatestTagTask : DefaultTask() {
    @TaskAction
    fun run() = println(git("describe", "--abbrev=0", "--tags"))
}

abstract class DiffFilesYesNoTask : DefaultTask() {
    @TaskAction
    fun run() {
        val branch = project.findProperty("branch") as String?
            ?: throw GradleException("Missing property: branch")
        val prefixes = (project.findProperty("pathPrefix") as String?)
            ?.split(",")?.filter { it.isNotBlank() }
            ?: throw GradleException("Missing property: pathPrefix")
        val files = git("diff", "--name-only", branch, ".").lines()
        val affected = files.any { file -> prefixes.any { file.startsWith(it) } }
        println(if (affected) "yes" else "no")
    }
}

abstract class MoveOutputsTask : DefaultTask() {
    @TaskAction
    fun run() {
        val versionName = System.getenv("APP_VERSION_NAME")
            ?: throw GradleException("Missing environment variable: APP_VERSION_NAME")
        val buildType = System.getenv("APP_BUILD_TYPE") ?: "release"
        val appDir = project.file("app")
        val distDir = project.file("dist").apply { mkdirs() }
        listOf("universal", "arm64-v8a", "armeabi-v7a", "x86_64", "x86").forEach { abi ->
            val source = File(appDir, "build/outputs/apk/$buildType/app-$abi-$buildType.apk")
            val target = File(distDir, "symphony-v$versionName-$abi.apk")
            if (source.exists()) source.copyTo(target, overwrite = true) else logger.warn("missing ${source.path}")
        }
        File(appDir, "build/outputs/bundle/$buildType/app-$buildType.aab").let { source ->
            val target = File(distDir, "symphony-v$versionName.aab")
            if (source.exists()) source.copyTo(target, overwrite = true) else logger.warn("missing ${source.path}")
        }
        val mapping = File(appDir, "build/outputs/mapping/$buildType/mapping.txt")
        val mappingZip = File(distDir, "mapping.zip")
        if (mapping.exists()) {
            ZipOutputStream(mappingZip.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry("mapping.txt"))
                mapping.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        } else {
            logger.warn("missing ${mapping.path}")
        }
    }
}

abstract class ChangelogLimitTask : DefaultTask() {
    @TaskAction
    fun run() {
        val limit = 540
        var failed = 0
        val root = project.file("metadata")
        root.walkTopDown()
            .filter { it.isFile && it.parentFile.name == "changelogs" }
            .forEach { file ->
                val length = file.readText().length
                if (length > limit) {
                    failed++
                    val relative = file.relativeTo(root).path
                    println("$relative has $length characters (excess by ${length - limit} characters)")
                }
            }
        if (failed > 0) throw GradleException("$failed changelog files exceed $limit characters")
    }
}
