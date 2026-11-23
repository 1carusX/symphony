package io.github.zyrouge.symphony.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.i18n.generated.*
import org.jetbrains.compose.resources.stringResource

@Immutable
class Strings(
    private val values: Map<String, String>,
    private val language: String,
) {
    val AddFolder: String get() = values.getValue("AddFolder")
    val AddSongs: String get() = values.getValue("AddSongs")
    val AddToPlaylist: String get() = values.getValue("AddToPlaylist")
    val AddToQueue: String get() = values.getValue("AddToQueue")
    val Album: String get() = values.getValue("Album")
    val AlbumArtist: String get() = values.getValue("AlbumArtist")
    val AlbumArtists: String get() = values.getValue("AlbumArtists")
    val Albums: String get() = values.getValue("Albums")
    val All: String get() = values.getValue("All")
    val Amber: String get() = values.getValue("Amber")
    val AppBuiltIn: String get() = values.getValue("AppBuiltIn")
    val Appearance: String get() = values.getValue("Appearance")
    val AreYouSureThatYouWantToDeleteThisPlaylist: String get() = values.getValue("AreYouSureThatYouWantToDeleteThisPlaylist")
    val Artist: String get() = values.getValue("Artist")
    val Artists: String get() = values.getValue("Artists")
    val ArtistTagValueSeparators: String get() = values.getValue("ArtistTagValueSeparators")
    val ArtworkQuality: String get() = values.getValue("ArtworkQuality")
    val AudioChannels: String get() = values.getValue("AudioChannels")
    val Bitrate: String get() = values.getValue("Bitrate")
    val Black: String get() = values.getValue("Black")
    val BlacklistFolders: String get() = values.getValue("BlacklistFolders")
    val Blue: String get() = values.getValue("Blue")
    val Browser: String get() = values.getValue("Browser")
    val Cancel: String get() = values.getValue("Cancel")
    val CaseSensitiveSorting: String get() = values.getValue("CaseSensitiveSorting")
    val CheckForUpdates: String get() = values.getValue("CheckForUpdates")
    val ClearSongCache: String get() = values.getValue("ClearSongCache")
    val Composer: String get() = values.getValue("Composer")
    val ConsiderContributing: String get() = values.getValue("ConsiderContributing")
    val ContentScale: String get() = values.getValue("ContentScale")
    val Custom: String get() = values.getValue("Custom")
    val Cyan: String get() = values.getValue("Cyan")
    val DamnThisIsSoEmpty: String get() = values.getValue("DamnThisIsSoEmpty")
    val Dark: String get() = values.getValue("Dark")
    val Date: String get() = values.getValue("Date")
    val Delete: String get() = values.getValue("Delete")
    val DeletePlaylist: String get() = values.getValue("DeletePlaylist")
    val Details: String get() = values.getValue("Details")
    val Disabled: String get() = values.getValue("Disabled")
    val DiscNumber: String get() = values.getValue("DiscNumber")
    val Discord: String get() = values.getValue("Discord")
    val DiscTotal: String get() = values.getValue("DiscTotal")
    val Done: String get() = values.getValue("Done")
    val Duration: String get() = values.getValue("Duration")
    val Emerald: String get() = values.getValue("Emerald")
    val Enabled: String get() = values.getValue("Enabled")
    val Encoder: String get() = values.getValue("Encoder")
    val Equalizer: String get() = values.getValue("Equalizer")
    val Export: String get() = values.getValue("Export")
    val FadePlaybackInOut: String get() = values.getValue("FadePlaybackInOut")
    val FastForwardDuration: String get() = values.getValue("FastForwardDuration")
    val FastRewindDuration: String get() = values.getValue("FastRewindDuration")
    val Favorite: String get() = values.getValue("Favorite")
    val FDroid: String get() = values.getValue("FDroid")
    val Filename: String get() = values.getValue("Filename")
    val FilteringResults: String get() = values.getValue("FilteringResults")
    val Folders: String get() = values.getValue("Folders")
    val Font: String get() = values.getValue("Font")
    val FontScale: String get() = values.getValue("FontScale")
    val ForYou: String get() = values.getValue("ForYou")
    val Fuchsia: String get() = values.getValue("Fuchsia")
    val GaplessPlayback: String get() = values.getValue("GaplessPlayback")
    val Genre: String get() = values.getValue("Genre")
    val Genres: String get() = values.getValue("Genres")
    val GenreTagValueSeparators: String get() = values.getValue("GenreTagValueSeparators")
    val Github: String get() = values.getValue("Github")
    val Green: String get() = values.getValue("Green")
    val GridColumns: String get() = values.getValue("GridColumns")
    val Groove: String get() = values.getValue("Groove")
    val HelloThere: String get() = values.getValue("HelloThere")
    val High: String get() = values.getValue("High")
    val HintAddMediaFolders: String get() = values.getValue("HintAddMediaFolders")
    val Home: String get() = values.getValue("Home")
    val HomeTabs: String get() = values.getValue("HomeTabs")
    val Hours: String get() = values.getValue("Hours")
    val Id: String get() = values.getValue("Id")
    val IgnoreAudioFocusLoss: String get() = values.getValue("IgnoreAudioFocusLoss")
    val ImportPlaylist: String get() = values.getValue("ImportPlaylist")
    val Indigo: String get() = values.getValue("Indigo")
    val IntroductoryMessage: String get() = values.getValue("IntroductoryMessage")
    val InvalidM3UFile: String get() = values.getValue("InvalidM3UFile")
    val IzzyOnDroid: String get() = values.getValue("IzzyOnDroid")
    val Language_: String get() = values.getValue("Language_")
    val LastModified: String get() = values.getValue("LastModified")
    val Length: String get() = values.getValue("Length")
    val Light: String get() = values.getValue("Light")
    val Lime: String get() = values.getValue("Lime")
    val Loading: String get() = values.getValue("Loading")
    val LocalStorage: String get() = values.getValue("LocalStorage")
    val Loseless: String get() = values.getValue("Loseless")
    val Low: String get() = values.getValue("Low")
    val Lyrics: String get() = values.getValue("Lyrics")
    val ManageSongs: String get() = values.getValue("ManageSongs")
    val MaterialYou: String get() = values.getValue("MaterialYou")
    val MaximumBitrate: String get() = values.getValue("MaximumBitrate")
    val MediaFolders: String get() = values.getValue("MediaFolders")
    val Medium: String get() = values.getValue("Medium")
    val MinimumBitrate: String get() = values.getValue("MinimumBitrate")
    val MiniPlayer: String get() = values.getValue("MiniPlayer")
    val MiniPlayerTextMarquee: String get() = values.getValue("MiniPlayerTextMarquee")
    val MinSongDurationFilter: String get() = values.getValue("MinSongDurationFilter")
    val Minutes: String get() = values.getValue("Minutes")
    val Name: String get() = values.getValue("Name")
    val NewPlaylist: String get() = values.getValue("NewPlaylist")
    val Next: String get() = values.getValue("Next")
    val No: String get() = values.getValue("No")
    val NoFoldersFound: String get() = values.getValue("NoFoldersFound")
    val NoInAppPlaylistsFound: String get() = values.getValue("NoInAppPlaylistsFound")
    val NoLocalPlaylistsFound: String get() = values.getValue("NoLocalPlaylistsFound")
    val NoLyrics: String get() = values.getValue("NoLyrics")
    val NoResultsFound: String get() = values.getValue("NoResultsFound")
    val NothingIsBeingPlayedRightNow: String get() = values.getValue("NothingIsBeingPlayedRightNow")
    val NowPlaying: String get() = values.getValue("NowPlaying")
    val Orange: String get() = values.getValue("Orange")
    val Path: String get() = values.getValue("Path")
    val Pause: String get() = values.getValue("Pause")
    val PauseOnCurrentSongEnd: String get() = values.getValue("PauseOnCurrentSongEnd")
    val PauseOnHeadphonesDisconnect: String get() = values.getValue("PauseOnHeadphonesDisconnect")
    val PersistUntilQueueEnd: String get() = values.getValue("PersistUntilQueueEnd")
    val PickFolder: String get() = values.getValue("PickFolder")
    val Pink: String get() = values.getValue("Pink")
    val Pitch: String get() = values.getValue("Pitch")
    val Play: String get() = values.getValue("Play")
    val PlayAll: String get() = values.getValue("PlayAll")
    val Player: String get() = values.getValue("Player")
    val Playlist: String get() = values.getValue("Playlist")
    val Playlists: String get() = values.getValue("Playlists")
    val PlaylistStoreLocation: String get() = values.getValue("PlaylistStoreLocation")
    val PlayNext: String get() = values.getValue("PlayNext")
    val PlayOnHeadphonesConnect: String get() = values.getValue("PlayOnHeadphonesConnect")
    val PlayStore: String get() = values.getValue("PlayStore")
    val Previous: String get() = values.getValue("Previous")
    val PrimaryColor: String get() = values.getValue("PrimaryColor")
    val Purple: String get() = values.getValue("Purple")
    val Queue: String get() = values.getValue("Queue")
    val QuitAppOnEnd: String get() = values.getValue("QuitAppOnEnd")
    val RecentlyAddedSongs: String get() = values.getValue("RecentlyAddedSongs")
    val Red: String get() = values.getValue("Red")
    val Reddit: String get() = values.getValue("Reddit")
    val RemoveFromPlaylist: String get() = values.getValue("RemoveFromPlaylist")
    val Rename: String get() = values.getValue("Rename")
    val RenamePlaylist: String get() = values.getValue("RenamePlaylist")
    val ReplaceArtwork: String get() = values.getValue("ReplaceArtwork")
    val ReportAnIssue: String get() = values.getValue("ReportAnIssue")
    val RequireAudioFocus: String get() = values.getValue("RequireAudioFocus")
    val Rescan: String get() = values.getValue("Rescan")
    val Reset: String get() = values.getValue("Reset")
    val Rose: String get() = values.getValue("Rose")
    val SamplingRate: String get() = values.getValue("SamplingRate")
    val SearchYourMusic: String get() = values.getValue("SearchYourMusic")
    val SelectAtleast2orAtmost5Tabs: String get() = values.getValue("SelectAtleast2orAtmost5Tabs")
    val SeparatePage: String get() = values.getValue("SeparatePage")
    val Settings: String get() = values.getValue("Settings")
    val ShareSong: String get() = values.getValue("ShareSong")
    val ShowAudioInformation: String get() = values.getValue("ShowAudioInformation")
    val ShowSeekControls: String get() = values.getValue("ShowSeekControls")
    val ShowTrackControls: String get() = values.getValue("ShowTrackControls")
    val ShowUpdateToast: String get() = values.getValue("ShowUpdateToast")
    val ShufflePlay: String get() = values.getValue("ShufflePlay")
    val Size: String get() = values.getValue("Size")
    val Sky: String get() = values.getValue("Sky")
    val SleepTimer: String get() = values.getValue("SleepTimer")
    val SongCacheCleared: String get() = values.getValue("SongCacheCleared")
    val Songs: String get() = values.getValue("Songs")
    val SongsFilterPattern: String get() = values.getValue("SongsFilterPattern")
    val Speed: String get() = values.getValue("Speed")
    val Stop: String get() = values.getValue("Stop")
    val SuggestedAlbumArtists: String get() = values.getValue("SuggestedAlbumArtists")
    val SuggestedAlbums: String get() = values.getValue("SuggestedAlbums")
    val SuggestedArtists: String get() = values.getValue("SuggestedArtists")
    val System: String get() = values.getValue("System")
    val SystemLightBlack: String get() = values.getValue("SystemLightBlack")
    val SystemLightDark: String get() = values.getValue("SystemLightDark")
    val Teal: String get() = values.getValue("Teal")
    val Theme: String get() = values.getValue("Theme")
    val Title: String get() = values.getValue("Title")
    val TotalSamples: String get() = values.getValue("TotalSamples")
    val TrackCount: String get() = values.getValue("TrackCount")
    val TrackName: String get() = values.getValue("TrackName")
    val TrackNumber: String get() = values.getValue("TrackNumber")
    val Traditional: String get() = values.getValue("Traditional")
    val Tree: String get() = values.getValue("Tree")
    val Unfavorite: String get() = values.getValue("Unfavorite")
    val UnknownSymbol: String get() = values.getValue("UnknownSymbol")
    val Updates: String get() = values.getValue("Updates")
    val UseMetaphonyMetadataDecoder: String get() = values.getValue("UseMetaphonyMetadataDecoder")
    val Variable: String get() = values.getValue("Variable")
    val VBR: String get() = values.getValue("VBR")
    val Version: String get() = values.getValue("Version")
    val ViewAlbum: String get() = values.getValue("ViewAlbum")
    val ViewAlbumArtist: String get() = values.getValue("ViewAlbumArtist")
    val ViewArtist: String get() = values.getValue("ViewArtist")
    val ViewGenre: String get() = values.getValue("ViewGenre")
    val Violet: String get() = values.getValue("Violet")
    val VisibleWhenActive: String get() = values.getValue("VisibleWhenActive")
    val WhitelistFolders: String get() = values.getValue("WhitelistFolders")
    val Year: String get() = values.getValue("Year")
    val Yellow: String get() = values.getValue("Yellow")
    val Yes: String get() = values.getValue("Yes")
    val MoreLikeThis: String get() = values.getValue("MoreLikeThis")
    val Tracks: String get() = values.getValue("Tracks")
    val Library: String get() = values.getValue("Library")
    val ColorScheme: String get() = values.getValue("ColorScheme")
    val AllGenres: String get() = values.getValue("AllGenres")
    val YourLibrary: String get() = values.getValue("YourLibrary")
    val UpNext: String get() = values.getValue("UpNext")
    val LateNight: String get() = values.getValue("LateNight")
    val ChooseMusicFolder: String get() = values.getValue("ChooseMusicFolder")
    val SymphonyDesktop: String get() = values.getValue("SymphonyDesktop")
    val OpenFolder: String get() = values.getValue("OpenFolder")
    val Resume: String get() = values.getValue("Resume")
    val Volume: String get() = values.getValue("Volume")
    val NothingPlaying: String get() = values.getValue("NothingPlaying")
    val LibVLCIsNotInstalled: String get() = values.getValue("LibVLCIsNotInstalled")
    val FileNotFound: String get() = values.getValue("FileNotFound")
    val FileIsNotReadable: String get() = values.getValue("FileIsNotReadable")
    val UnsupportedFormat: String get() = values.getValue("UnsupportedFormat")
    val PlaybackFailed: String get() = values.getValue("PlaybackFailed")
    val LibraryFolders: String get() = values.getValue("LibraryFolders")
    val SortSongsBy: String get() = values.getValue("SortSongsBy")
    val More: String get() = values.getValue("More")

    fun CopiedXToClipboard(x: String): String = values.getValue("CopiedXToClipboard").format(x)
    fun ExportedX(x: String): String = values.getValue("ExportedX").format(x)
    fun ExportFailedX(x: String): String = values.getValue("ExportFailedX").format(x)
    fun LaunchingEqualizerFailedX(x: String): String = values.getValue("LaunchingEqualizerFailedX").format(x)
    fun NewVersionAvailableX(x: String): String = values.getValue("NewVersionAvailableX").format(x)
    fun PlayingXofY(x: String, y: String): String = values.getValue("PlayingXofY").format(x, y)
    fun ShareFailedX(x: String): String = values.getValue("ShareFailedX").format(x)
    fun UnknownAlbumX(x: String): String = values.getValue("UnknownAlbumX").format(x)
    fun UnknownArtistX(x: String): String = values.getValue("UnknownArtistX").format(x)
    fun UnknownGenreX(x: String): String = values.getValue("UnknownGenreX").format(x)
    fun UnknownPlaylistX(x: String): String = values.getValue("UnknownPlaylistX").format(x)
    fun XChannels(x: String): String = values.getValue("XChannels").format(x)
    fun PlayModeX(x: String): String = values.getValue("PlayModeX").format(x)

    fun XAlbums(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XAlbums_one"],
        other = values.getValue("XAlbums_other"),
    ).format(x)

    fun XArtists(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XArtists_one"],
        other = values.getValue("XArtists_other"),
    ).format(x)

    fun XBit(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XBit_one"],
        other = values.getValue("XBit_other"),
    ).format(x)

    fun XFolders(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XFolders_one"],
        other = values.getValue("XFolders_other"),
    ).format(x)

    fun XFoldersYFiles(x: String, y: String): String = values.getValue("XFoldersYFiles_other").format(x, y)

    fun XGenres(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XGenres_one"],
        other = values.getValue("XGenres_other"),
    ).format(x)

    fun XItems(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XItems_one"],
        other = values.getValue("XItems_other"),
    ).format(x)

    fun XKbps(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XKbps_one"],
        other = values.getValue("XKbps_other"),
    ).format(x)

    fun XKHz(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XKHz_one"],
        other = values.getValue("XKHz_other"),
    ).format(x)

    fun XPlaylists(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XPlaylists_one"],
        other = values.getValue("XPlaylists_other"),
    ).format(x)

    fun XSecs(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XSecs_one"],
        other = values.getValue("XSecs_other"),
    ).format(x)

    fun XSongs(x: String): String = pluralTemplate(
        language = language,
        count = x,
        one = values["XSongs_one"],
        other = values.getValue("XSongs_other"),
    ).format(x)
}

@Composable
fun rememberStrings(language: String): Strings = Strings(
    language = language,
    values = mapOf(
        "AddFolder" to stringResource(Res.string.AddFolder),
        "AddSongs" to stringResource(Res.string.AddSongs),
        "AddToPlaylist" to stringResource(Res.string.AddToPlaylist),
        "AddToQueue" to stringResource(Res.string.AddToQueue),
        "Album" to stringResource(Res.string.Album),
        "AlbumArtist" to stringResource(Res.string.AlbumArtist),
        "AlbumArtists" to stringResource(Res.string.AlbumArtists),
        "Albums" to stringResource(Res.string.Albums),
        "All" to stringResource(Res.string.All),
        "Amber" to stringResource(Res.string.Amber),
        "AppBuiltIn" to stringResource(Res.string.AppBuiltIn),
        "Appearance" to stringResource(Res.string.Appearance),
        "AreYouSureThatYouWantToDeleteThisPlaylist" to stringResource(Res.string.AreYouSureThatYouWantToDeleteThisPlaylist),
        "Artist" to stringResource(Res.string.Artist),
        "Artists" to stringResource(Res.string.Artists),
        "ArtistTagValueSeparators" to stringResource(Res.string.ArtistTagValueSeparators),
        "ArtworkQuality" to stringResource(Res.string.ArtworkQuality),
        "AudioChannels" to stringResource(Res.string.AudioChannels),
        "Bitrate" to stringResource(Res.string.Bitrate),
        "Black" to stringResource(Res.string.Black),
        "BlacklistFolders" to stringResource(Res.string.BlacklistFolders),
        "Blue" to stringResource(Res.string.Blue),
        "Browser" to stringResource(Res.string.Browser),
        "Cancel" to stringResource(Res.string.Cancel),
        "CaseSensitiveSorting" to stringResource(Res.string.CaseSensitiveSorting),
        "CheckForUpdates" to stringResource(Res.string.CheckForUpdates),
        "ClearSongCache" to stringResource(Res.string.ClearSongCache),
        "Composer" to stringResource(Res.string.Composer),
        "ConsiderContributing" to stringResource(Res.string.ConsiderContributing),
        "ContentScale" to stringResource(Res.string.ContentScale),
        "CopiedXToClipboard" to stringResource(Res.string.CopiedXToClipboard),
        "Custom" to stringResource(Res.string.Custom),
        "Cyan" to stringResource(Res.string.Cyan),
        "DamnThisIsSoEmpty" to stringResource(Res.string.DamnThisIsSoEmpty),
        "Dark" to stringResource(Res.string.Dark),
        "Date" to stringResource(Res.string.Date),
        "Delete" to stringResource(Res.string.Delete),
        "DeletePlaylist" to stringResource(Res.string.DeletePlaylist),
        "Details" to stringResource(Res.string.Details),
        "Disabled" to stringResource(Res.string.Disabled),
        "DiscNumber" to stringResource(Res.string.DiscNumber),
        "Discord" to stringResource(Res.string.Discord),
        "DiscTotal" to stringResource(Res.string.DiscTotal),
        "Done" to stringResource(Res.string.Done),
        "Duration" to stringResource(Res.string.Duration),
        "Emerald" to stringResource(Res.string.Emerald),
        "Enabled" to stringResource(Res.string.Enabled),
        "Encoder" to stringResource(Res.string.Encoder),
        "Equalizer" to stringResource(Res.string.Equalizer),
        "Export" to stringResource(Res.string.Export),
        "ExportedX" to stringResource(Res.string.ExportedX),
        "ExportFailedX" to stringResource(Res.string.ExportFailedX),
        "FadePlaybackInOut" to stringResource(Res.string.FadePlaybackInOut),
        "FastForwardDuration" to stringResource(Res.string.FastForwardDuration),
        "FastRewindDuration" to stringResource(Res.string.FastRewindDuration),
        "Favorite" to stringResource(Res.string.Favorite),
        "FDroid" to stringResource(Res.string.FDroid),
        "Filename" to stringResource(Res.string.Filename),
        "FilteringResults" to stringResource(Res.string.FilteringResults),
        "Folders" to stringResource(Res.string.Folders),
        "Font" to stringResource(Res.string.Font),
        "FontScale" to stringResource(Res.string.FontScale),
        "ForYou" to stringResource(Res.string.ForYou),
        "Fuchsia" to stringResource(Res.string.Fuchsia),
        "GaplessPlayback" to stringResource(Res.string.GaplessPlayback),
        "Genre" to stringResource(Res.string.Genre),
        "Genres" to stringResource(Res.string.Genres),
        "GenreTagValueSeparators" to stringResource(Res.string.GenreTagValueSeparators),
        "Github" to stringResource(Res.string.Github),
        "Green" to stringResource(Res.string.Green),
        "GridColumns" to stringResource(Res.string.GridColumns),
        "Groove" to stringResource(Res.string.Groove),
        "HelloThere" to stringResource(Res.string.HelloThere),
        "High" to stringResource(Res.string.High),
        "HintAddMediaFolders" to stringResource(Res.string.HintAddMediaFolders),
        "Home" to stringResource(Res.string.Home),
        "HomeTabs" to stringResource(Res.string.HomeTabs),
        "Hours" to stringResource(Res.string.Hours),
        "Id" to stringResource(Res.string.Id),
        "IgnoreAudioFocusLoss" to stringResource(Res.string.IgnoreAudioFocusLoss),
        "ImportPlaylist" to stringResource(Res.string.ImportPlaylist),
        "Indigo" to stringResource(Res.string.Indigo),
        "IntroductoryMessage" to stringResource(Res.string.IntroductoryMessage),
        "InvalidM3UFile" to stringResource(Res.string.InvalidM3UFile),
        "IzzyOnDroid" to stringResource(Res.string.IzzyOnDroid),
        "Language_" to stringResource(Res.string.Language_),
        "LastModified" to stringResource(Res.string.LastModified),
        "LaunchingEqualizerFailedX" to stringResource(Res.string.LaunchingEqualizerFailedX),
        "Length" to stringResource(Res.string.Length),
        "Light" to stringResource(Res.string.Light),
        "Lime" to stringResource(Res.string.Lime),
        "Loading" to stringResource(Res.string.Loading),
        "LocalStorage" to stringResource(Res.string.LocalStorage),
        "Loseless" to stringResource(Res.string.Loseless),
        "Low" to stringResource(Res.string.Low),
        "Lyrics" to stringResource(Res.string.Lyrics),
        "ManageSongs" to stringResource(Res.string.ManageSongs),
        "MaterialYou" to stringResource(Res.string.MaterialYou),
        "MaximumBitrate" to stringResource(Res.string.MaximumBitrate),
        "MediaFolders" to stringResource(Res.string.MediaFolders),
        "Medium" to stringResource(Res.string.Medium),
        "MinimumBitrate" to stringResource(Res.string.MinimumBitrate),
        "MiniPlayer" to stringResource(Res.string.MiniPlayer),
        "MiniPlayerTextMarquee" to stringResource(Res.string.MiniPlayerTextMarquee),
        "MinSongDurationFilter" to stringResource(Res.string.MinSongDurationFilter),
        "Minutes" to stringResource(Res.string.Minutes),
        "Name" to stringResource(Res.string.Name),
        "NewPlaylist" to stringResource(Res.string.NewPlaylist),
        "NewVersionAvailableX" to stringResource(Res.string.NewVersionAvailableX),
        "Next" to stringResource(Res.string.Next),
        "No" to stringResource(Res.string.No),
        "NoFoldersFound" to stringResource(Res.string.NoFoldersFound),
        "NoInAppPlaylistsFound" to stringResource(Res.string.NoInAppPlaylistsFound),
        "NoLocalPlaylistsFound" to stringResource(Res.string.NoLocalPlaylistsFound),
        "NoLyrics" to stringResource(Res.string.NoLyrics),
        "NoResultsFound" to stringResource(Res.string.NoResultsFound),
        "NothingIsBeingPlayedRightNow" to stringResource(Res.string.NothingIsBeingPlayedRightNow),
        "NowPlaying" to stringResource(Res.string.NowPlaying),
        "Orange" to stringResource(Res.string.Orange),
        "Path" to stringResource(Res.string.Path),
        "Pause" to stringResource(Res.string.Pause),
        "PauseOnCurrentSongEnd" to stringResource(Res.string.PauseOnCurrentSongEnd),
        "PauseOnHeadphonesDisconnect" to stringResource(Res.string.PauseOnHeadphonesDisconnect),
        "PersistUntilQueueEnd" to stringResource(Res.string.PersistUntilQueueEnd),
        "PickFolder" to stringResource(Res.string.PickFolder),
        "Pink" to stringResource(Res.string.Pink),
        "Pitch" to stringResource(Res.string.Pitch),
        "Play" to stringResource(Res.string.Play),
        "PlayAll" to stringResource(Res.string.PlayAll),
        "Player" to stringResource(Res.string.Player),
        "PlayingXofY" to stringResource(Res.string.PlayingXofY),
        "Playlist" to stringResource(Res.string.Playlist),
        "Playlists" to stringResource(Res.string.Playlists),
        "PlaylistStoreLocation" to stringResource(Res.string.PlaylistStoreLocation),
        "PlayNext" to stringResource(Res.string.PlayNext),
        "PlayOnHeadphonesConnect" to stringResource(Res.string.PlayOnHeadphonesConnect),
        "PlayStore" to stringResource(Res.string.PlayStore),
        "Previous" to stringResource(Res.string.Previous),
        "PrimaryColor" to stringResource(Res.string.PrimaryColor),
        "Purple" to stringResource(Res.string.Purple),
        "Queue" to stringResource(Res.string.Queue),
        "QuitAppOnEnd" to stringResource(Res.string.QuitAppOnEnd),
        "RecentlyAddedSongs" to stringResource(Res.string.RecentlyAddedSongs),
        "Red" to stringResource(Res.string.Red),
        "Reddit" to stringResource(Res.string.Reddit),
        "RemoveFromPlaylist" to stringResource(Res.string.RemoveFromPlaylist),
        "Rename" to stringResource(Res.string.Rename),
        "RenamePlaylist" to stringResource(Res.string.RenamePlaylist),
        "ReplaceArtwork" to stringResource(Res.string.ReplaceArtwork),
        "ReportAnIssue" to stringResource(Res.string.ReportAnIssue),
        "RequireAudioFocus" to stringResource(Res.string.RequireAudioFocus),
        "Rescan" to stringResource(Res.string.Rescan),
        "Reset" to stringResource(Res.string.Reset),
        "Rose" to stringResource(Res.string.Rose),
        "SamplingRate" to stringResource(Res.string.SamplingRate),
        "SearchYourMusic" to stringResource(Res.string.SearchYourMusic),
        "SelectAtleast2orAtmost5Tabs" to stringResource(Res.string.SelectAtleast2orAtmost5Tabs),
        "SeparatePage" to stringResource(Res.string.SeparatePage),
        "Settings" to stringResource(Res.string.Settings),
        "ShareFailedX" to stringResource(Res.string.ShareFailedX),
        "ShareSong" to stringResource(Res.string.ShareSong),
        "ShowAudioInformation" to stringResource(Res.string.ShowAudioInformation),
        "ShowSeekControls" to stringResource(Res.string.ShowSeekControls),
        "ShowTrackControls" to stringResource(Res.string.ShowTrackControls),
        "ShowUpdateToast" to stringResource(Res.string.ShowUpdateToast),
        "ShufflePlay" to stringResource(Res.string.ShufflePlay),
        "Size" to stringResource(Res.string.Size),
        "Sky" to stringResource(Res.string.Sky),
        "SleepTimer" to stringResource(Res.string.SleepTimer),
        "SongCacheCleared" to stringResource(Res.string.SongCacheCleared),
        "Songs" to stringResource(Res.string.Songs),
        "SongsFilterPattern" to stringResource(Res.string.SongsFilterPattern),
        "Speed" to stringResource(Res.string.Speed),
        "Stop" to stringResource(Res.string.Stop),
        "SuggestedAlbumArtists" to stringResource(Res.string.SuggestedAlbumArtists),
        "SuggestedAlbums" to stringResource(Res.string.SuggestedAlbums),
        "SuggestedArtists" to stringResource(Res.string.SuggestedArtists),
        "System" to stringResource(Res.string.System),
        "SystemLightBlack" to stringResource(Res.string.SystemLightBlack),
        "SystemLightDark" to stringResource(Res.string.SystemLightDark),
        "Teal" to stringResource(Res.string.Teal),
        "Theme" to stringResource(Res.string.Theme),
        "Title" to stringResource(Res.string.Title),
        "TotalSamples" to stringResource(Res.string.TotalSamples),
        "TrackCount" to stringResource(Res.string.TrackCount),
        "TrackName" to stringResource(Res.string.TrackName),
        "TrackNumber" to stringResource(Res.string.TrackNumber),
        "Traditional" to stringResource(Res.string.Traditional),
        "Tree" to stringResource(Res.string.Tree),
        "Unfavorite" to stringResource(Res.string.Unfavorite),
        "UnknownAlbumX" to stringResource(Res.string.UnknownAlbumX),
        "UnknownArtistX" to stringResource(Res.string.UnknownArtistX),
        "UnknownGenreX" to stringResource(Res.string.UnknownGenreX),
        "UnknownPlaylistX" to stringResource(Res.string.UnknownPlaylistX),
        "UnknownSymbol" to stringResource(Res.string.UnknownSymbol),
        "Updates" to stringResource(Res.string.Updates),
        "UseMetaphonyMetadataDecoder" to stringResource(Res.string.UseMetaphonyMetadataDecoder),
        "Variable" to stringResource(Res.string.Variable),
        "VBR" to stringResource(Res.string.VBR),
        "Version" to stringResource(Res.string.Version),
        "ViewAlbum" to stringResource(Res.string.ViewAlbum),
        "ViewAlbumArtist" to stringResource(Res.string.ViewAlbumArtist),
        "ViewArtist" to stringResource(Res.string.ViewArtist),
        "ViewGenre" to stringResource(Res.string.ViewGenre),
        "Violet" to stringResource(Res.string.Violet),
        "VisibleWhenActive" to stringResource(Res.string.VisibleWhenActive),
        "WhitelistFolders" to stringResource(Res.string.WhitelistFolders),
        "XAlbums_other" to stringResource(Res.string.XAlbums_other),
        "XAlbums_one" to stringResource(Res.string.XAlbums_one),
        "XArtists_other" to stringResource(Res.string.XArtists_other),
        "XArtists_one" to stringResource(Res.string.XArtists_one),
        "XBit_other" to stringResource(Res.string.XBit_other),
        "XBit_one" to stringResource(Res.string.XBit_one),
        "XChannels" to stringResource(Res.string.XChannels),
        "XFolders_other" to stringResource(Res.string.XFolders_other),
        "XFolders_one" to stringResource(Res.string.XFolders_one),
        "XFoldersYFiles_other" to stringResource(Res.string.XFoldersYFiles_other),
        "XFoldersYFiles_one" to stringResource(Res.string.XFoldersYFiles_one),
        "XGenres_other" to stringResource(Res.string.XGenres_other),
        "XGenres_one" to stringResource(Res.string.XGenres_one),
        "XItems_other" to stringResource(Res.string.XItems_other),
        "XItems_one" to stringResource(Res.string.XItems_one),
        "XKbps_other" to stringResource(Res.string.XKbps_other),
        "XKbps_one" to stringResource(Res.string.XKbps_one),
        "XKHz_other" to stringResource(Res.string.XKHz_other),
        "XKHz_one" to stringResource(Res.string.XKHz_one),
        "XPlaylists_other" to stringResource(Res.string.XPlaylists_other),
        "XPlaylists_one" to stringResource(Res.string.XPlaylists_one),
        "XSecs_other" to stringResource(Res.string.XSecs_other),
        "XSecs_one" to stringResource(Res.string.XSecs_one),
        "XSongs_other" to stringResource(Res.string.XSongs_other),
        "XSongs_one" to stringResource(Res.string.XSongs_one),
        "Year" to stringResource(Res.string.Year),
        "Yellow" to stringResource(Res.string.Yellow),
        "Yes" to stringResource(Res.string.Yes),
        "MoreLikeThis" to stringResource(Res.string.MoreLikeThis),
        "Tracks" to stringResource(Res.string.Tracks),
        "Library" to stringResource(Res.string.Library),
        "ColorScheme" to stringResource(Res.string.ColorScheme),
        "AllGenres" to stringResource(Res.string.AllGenres),
        "YourLibrary" to stringResource(Res.string.YourLibrary),
        "UpNext" to stringResource(Res.string.UpNext),
        "LateNight" to stringResource(Res.string.LateNight),
        "ChooseMusicFolder" to stringResource(Res.string.ChooseMusicFolder),
        "SymphonyDesktop" to stringResource(Res.string.SymphonyDesktop),
        "OpenFolder" to stringResource(Res.string.OpenFolder),
        "Resume" to stringResource(Res.string.Resume),
        "Volume" to stringResource(Res.string.Volume),
        "NothingPlaying" to stringResource(Res.string.NothingPlaying),
        "LibVLCIsNotInstalled" to stringResource(Res.string.LibVLCIsNotInstalled),
        "FileNotFound" to stringResource(Res.string.FileNotFound),
        "FileIsNotReadable" to stringResource(Res.string.FileIsNotReadable),
        "UnsupportedFormat" to stringResource(Res.string.UnsupportedFormat),
        "PlaybackFailed" to stringResource(Res.string.PlaybackFailed),
        "LibraryFolders" to stringResource(Res.string.LibraryFolders),
        "SortSongsBy" to stringResource(Res.string.SortSongsBy),
        "PlayModeX" to stringResource(Res.string.PlayModeX),
        "More" to stringResource(Res.string.More),
    ),
)
