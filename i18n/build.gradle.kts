plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "io.github.zyrouge.symphony.i18n"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()
        androidResources.enable = true
    }

    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    jvmToolchain(17)

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.components.resources)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.github.zyrouge.symphony.i18n.generated"
    generateResClass = auto
}

val i18nDir = layout.projectDirectory.dir("src/commonMain/composeResources")

val verifyI18n by tasks.registering {
    inputs.dir(i18nDir)
    doLast {
        fun readFile(file: File): Pair<Set<String>, Set<String>> {
            val document = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file)
            val strings = mutableSetOf<String>()
            val plurals = mutableSetOf<String>()
            val stringNodes = document.getElementsByTagName("string")
            for (index in 0 until stringNodes.length) {
                strings += (stringNodes.item(index) as org.w3c.dom.Element).getAttribute("name")
            }
            val pluralNodes = document.getElementsByTagName("plurals")
            for (index in 0 until pluralNodes.length) {
                plurals += (pluralNodes.item(index) as org.w3c.dom.Element).getAttribute("name")
            }
            return strings to plurals
        }

        val dir = i18nDir.asFile
        val base = readFile(File(dir, "values/strings.xml"))
        val problems = mutableListOf<String>()
        dir.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .sortedBy { it.name }
            .forEach { locale ->
                val file = File(locale, "strings.xml")
                if (!file.exists()) {
                    problems += "${locale.name} has no strings.xml"
                    return@forEach
                }
                val (strings, _) = readFile(file)
                base.second.forEach { plural ->
                    if (!strings.contains("${plural}_other")) {
                        problems += "${locale.name} is missing ${plural}_other"
                    }
                }
                base.first
                    .filterNot { it.endsWith("_one") || it.endsWith("_other") }
                    .filterNot { strings.contains(it) }
                    .forEach { problems += "${locale.name} is missing $it" }
            }
        val localesFile = rootProject.file(
            "app/src/main/java/io/github/zyrouge/symphony/services/locales/Locales.kt",
        )
        if (localesFile.exists()) {
            val text = localesFile.readText()
            val declared = Regex("LocaleInfo\\(\"([^\"]+)\"")
                .findAll(text)
                .map { it.groupValues[1] }
                .toMutableSet()
            Regex("qualifier = \"([^\"]+)\"")
                .findAll(text)
                .map { it.groupValues[1] }
                .forEach { declared += it }
            val chunkDirs = dir.listFiles().orEmpty()
                .filter { it.isDirectory && it.name.startsWith("values-") }
                .map { it.name.removePrefix("values-") }
            chunkDirs
                .filterNot { declared.contains(it) }
                .forEach { problems += "Locales.kt has no entry for values-$it" }
        }
        if (problems.isNotEmpty()) {
            throw GradleException(problems.joinToString("\n"))
        }
    }
}

tasks.matching { it.name == "assemble" || it.name == "check" }.configureEach {
    dependsOn(verifyI18n)
}
