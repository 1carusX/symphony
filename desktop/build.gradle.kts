import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()

    jvmToolchain(21)

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
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
                debMaintainer = "zyrouge@github"
            }
        }

        buildTypes.release.proguard {
            obfuscate.set(false)
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.register<JavaExec>("smokeUi") {
    group = "verification"
    description = "Opens the desktop window, clicks the first track and captures evidence screenshots."
    val testCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")
    classpath = files(testCompilation.output.allOutputs, testCompilation.runtimeDependencyFiles)
    mainClass.set("io.github.zyrouge.symphony.desktop.ui.DesktopUiSmokeKt")
    val uiSmokeDir = (findProperty("uiSmokeDir") as String?) ?: "$rootDir/app/src/test/resources"
    args(uiSmokeDir, "$rootDir/.omo/evidence/symphony-d3")
}

tasks.register<JavaExec>("smokePlayback") {
    group = "verification"
    description = "Plays the bundled mp3 fixture through the vlcj backend for manual output checks."
    val testCompilation = kotlin.targets.getByName("jvm").compilations.getByName("test")
    classpath = files(testCompilation.output.allOutputs, testCompilation.runtimeDependencyFiles)
    mainClass.set("io.github.zyrouge.symphony.desktop.player.PlayerSmokeKt")
    args("$rootDir/app/src/test/resources/audio.mp3")
}

tasks.register("test") {
    group = "verification"
    description = "Alias for jvmTest, matching the plain JVM module convention."
    dependsOn("jvmTest")
}
