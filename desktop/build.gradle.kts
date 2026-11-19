import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
