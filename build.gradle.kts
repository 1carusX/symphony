plugins {
    alias(libs.plugins.android.app) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

tasks.register<VersionPrintTask>("versionPrint")
tasks.register<VersionBumpTask>("versionBump")
tasks.register<VersionPrereleaseTask>("versionPrintNightly")
tasks.register<VersionPrereleaseTask>("versionPrintCanary")
tasks.register<TagExistsTask>("tagExists")
tasks.register<TagExistsYesNoTask>("tagExistsYn")
tasks.register<LatestTagTask>("latestTag")
tasks.register<DiffFilesYesNoTask>("diffFilesYn")
tasks.register<MoveOutputsTask>("moveOutputs")
tasks.register<ChangelogLimitTask>("changelogLimit")
