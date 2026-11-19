import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val javafxLinux = configurations.detachedConfiguration(
    dependencies.create("org.openjfx:javafx-base:26:linux"),
    dependencies.create("org.openjfx:javafx-graphics:26:linux"),
    dependencies.create("org.openjfx:javafx-media:26:linux"),
)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    jvmToolchain(21)

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(compose.desktop.currentOs)
                // T2 player-backend spike dependencies (removed in T3).
                implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
                implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
                implementation("org.jflac:jflac-codec:1.5.2")
                implementation(files(javafxLinux))
                implementation("uk.co.caprica:vlcj:4.12.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.zyrouge.symphony.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Deb)

            linux {
                packageName = "symphony-desktop"
                packageVersion = "0.1.0"
            }
        }

        buildTypes.release.proguard {
            obfuscate.set(false)
        }
    }
}

private fun spikeTask(name: String, mainClassName: String) = tasks.register<JavaExec>(name) {
    group = "verification"
    mainClass.set(mainClassName)
    val compilation = kotlin.targets.getByName("jvm").compilations.getByName("main")
    classpath = files(compilation.runtimeDependencyFiles, compilation.output.allOutputs)
    workingDir = rootDir
    args("app/src/test/resources")
}

spikeTask("spikeJavaSound", "io.github.zyrouge.symphony.desktop.spike.JavaSoundProbeKt")
spikeTask("spikeJavaFx", "io.github.zyrouge.symphony.desktop.spike.JavaFxProbeKt")
spikeTask("spikeVlc", "io.github.zyrouge.symphony.desktop.spike.VlcProbeKt")

tasks.named<JavaExec>("spikeJavaFx") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        },
    )
}
