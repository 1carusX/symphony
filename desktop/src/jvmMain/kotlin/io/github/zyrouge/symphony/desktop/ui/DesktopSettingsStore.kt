package io.github.zyrouge.symphony.desktop.ui

import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/** Desktop appearance settings. */
data class DesktopSettings(
    val themeMode: BentoThemeMode = BentoThemeMode.SYSTEM,
    val colorScheme: BentoColorScheme = BentoColorScheme.TINTED,
    val primaryHue: BentoPrimaryHue = BentoPrimaryHue.ROSE,
    val fontScale: Float = 1f,
    val contentScale: Float = 1f,
    val language: String? = null,
)

/** Stores the desktop appearance settings in the XDG config directory. */
class DesktopSettingsStore(
    private val file: Path = defaultSettingsFile(),
) {
    fun load(): DesktopSettings = runCatching {
        if (!Files.isRegularFile(file)) return DesktopSettings()
        val properties = Properties()
        Files.newInputStream(file).use(properties::load)
        DesktopSettings(
            themeMode = properties.enumValue(
                "themeMode",
                BentoThemeMode.entries,
                BentoThemeMode.SYSTEM,
            ),
            colorScheme = properties.enumValue(
                "colorScheme",
                BentoColorScheme.entries,
                BentoColorScheme.TINTED,
            ),
            primaryHue = properties.enumValue(
                "primaryHue",
                BentoPrimaryHue.entries,
                BentoPrimaryHue.ROSE,
            ),
            fontScale = properties.getProperty("fontScale")?.toFloatOrNull() ?: 1f,
            contentScale = properties.getProperty("contentScale")?.toFloatOrNull() ?: 1f,
            language = properties.getProperty("language")?.takeIf { it.isNotBlank() },
        )
    }.getOrElse { DesktopSettings() }

    fun save(settings: DesktopSettings) {
        runCatching {
            val properties = Properties()
            properties.setProperty("themeMode", settings.themeMode.name)
            properties.setProperty("colorScheme", settings.colorScheme.name)
            properties.setProperty("primaryHue", settings.primaryHue.name)
            properties.setProperty("fontScale", settings.fontScale.toString())
            properties.setProperty("contentScale", settings.contentScale.toString())
            settings.language?.let { properties.setProperty("language", it) }
            file.parent?.let { Files.createDirectories(it) }
            Files.newOutputStream(file).use { stream ->
                properties.store(stream, "Symphony desktop settings")
            }
        }
    }

    companion object {
        fun defaultSettingsFile(): Path {
            val configHome = System.getenv("XDG_CONFIG_HOME")
                ?.takeIf { it.isNotBlank() }
                ?: (System.getProperty("user.home") + "/.config")
            return Path.of(configHome, "symphony", "settings.properties")
        }
    }
}

private fun <T : Enum<T>> Properties.enumValue(
    key: String,
    entries: List<T>,
    fallback: T,
): T {
    val value = getProperty(key) ?: return fallback
    return entries.firstOrNull { it.name == value } ?: fallback
}
