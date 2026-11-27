package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.desktop.ui.DesktopSettings
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.contracts.UiApp
import io.github.zyrouge.symphony.ui.contracts.UiCapabilities
import io.github.zyrouge.symphony.ui.contracts.UiHomePage
import io.github.zyrouge.symphony.ui.contracts.UiNavigation
import io.github.zyrouge.symphony.ui.contracts.UiPlatformServices
import io.github.zyrouge.symphony.ui.contracts.UiRoute
import io.github.zyrouge.symphony.ui.contracts.UiSettingsController
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import java.awt.Desktop
import java.net.URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopUiSettingsController(
    initial: DesktopSettings = DesktopSettings(),
    private val onChanged: (DesktopSettings) -> Unit = {},
) : UiSettingsController {
    override val themeMode = MutableStateFlow(initial.themeMode)
    override val colorScheme = MutableStateFlow(initial.colorScheme)
    override val primaryHue = MutableStateFlow(initial.primaryHue)
    override val fontScale = MutableStateFlow(initial.fontScale)
    override val contentScale = MutableStateFlow(initial.contentScale)
    override val language = MutableStateFlow(initial.language)
    override val homeTabs = MutableStateFlow(
        listOf(
            UiHomePage.FOR_YOU,
            UiHomePage.SONGS,
            UiHomePage.ALBUMS,
            UiHomePage.ARTISTS,
            UiHomePage.GENRES,
            UiHomePage.PLAYLISTS,
            UiHomePage.FOLDERS,
        ),
    )

    fun setThemeMode(value: BentoThemeMode) {
        themeMode.value = value
        notifyChanged()
    }

    fun setColorScheme(value: BentoColorScheme) {
        colorScheme.value = value
        notifyChanged()
    }

    fun setPrimaryHue(value: BentoPrimaryHue) {
        primaryHue.value = value
        notifyChanged()
    }

    fun setFontScale(value: Float) {
        fontScale.value = value
        notifyChanged()
    }

    fun setContentScale(value: Float) {
        contentScale.value = value
        notifyChanged()
    }

    fun setLanguage(value: String?) {
        language.value = value
        notifyChanged()
    }

    private fun notifyChanged() {
        onChanged(
            DesktopSettings(
                themeMode = themeMode.value,
                colorScheme = colorScheme.value,
                primaryHue = primaryHue.value,
                fontScale = fontScale.value,
                contentScale = contentScale.value,
                language = language.value,
            ),
        )
    }
}

class DesktopUiNavigation : UiNavigation {
    private val stack = ArrayDeque<UiRoute>().apply { add(UiRoute.Home) }
    private val _currentRoute = MutableStateFlow<UiRoute?>(UiRoute.Home)
    override val currentRoute = _currentRoute.asStateFlow()

    override fun navigate(route: UiRoute) {
        stack.add(route)
        _currentRoute.value = route
    }

    override fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeLast()
        _currentRoute.value = stack.last()
        return true
    }
}

class DesktopUiPlatformServices : UiPlatformServices {
    override val capabilities = UiCapabilities(
        canPickMusicFolder = true,
        hasSafFolders = false,
        hasAudioFocus = false,
        hasMediaSession = false,
        hasNotificationPlayer = false,
        hasSystemWallpaperColors = false,
    )

    override fun showMessage(text: String) {
        println(text)
    }

    override fun openUrl(url: String) {
        runCatching { Desktop.getDesktop().browse(URI(url)) }
    }

    override fun setKeepScreenAwake(enabled: Boolean) = Unit
}

class DesktopUiApp(
    scope: CoroutineScope,
    initialStrings: Strings,
    initialPlaylists: List<io.github.zyrouge.symphony.ui.model.UiPlaylist> = emptyList(),
    onPlaylistsChanged: (List<io.github.zyrouge.symphony.ui.model.UiPlaylist>) -> Unit = {},
    initialSettings: DesktopSettings = DesktopSettings(),
    onSettingsChanged: (DesktopSettings) -> Unit = {},
) : UiApp {
    private val _strings = MutableStateFlow(initialStrings)
    override val strings: StateFlow<Strings> = _strings.asStateFlow()

    override val library = DesktopUiLibraryController(scope, initialPlaylists, onPlaylistsChanged)
    override val playback: DesktopUiPlaybackController = DesktopUiPlaybackController(library, scope)
    override val settings: DesktopUiSettingsController = DesktopUiSettingsController(
        initial = initialSettings,
        onChanged = onSettingsChanged,
    )
    override val navigation: UiNavigation = DesktopUiNavigation()
    override val platform: UiPlatformServices = DesktopUiPlatformServices()

    fun updateStrings(value: Strings) {
        _strings.value = value
    }

    fun close() {
        playback.close()
    }
}
