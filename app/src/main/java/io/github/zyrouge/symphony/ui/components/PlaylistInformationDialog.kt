package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun PlaylistInformationDialog(
    context: ViewContext,
    playlist: Playlist,
    onDismissRequest: () -> Unit,
) {
    InformationDialog(
        context,
        header = {
            InformationHeader(
                image = playlist.createArtworkImageRequest(context.symphony).build(),
                title = { LongPressCopyableText(context, playlist.title) },
                subtitle = context.symphony.t.XSongs(playlist.numberOfTracks.toString()),
            )
        },
        content = {
            InformationKeyValue(context.symphony.t.TrackCount) {
                LongPressCopyableText(context, playlist.numberOfTracks.toString())
            }
            InformationKeyValue(context.symphony.t.PlaylistStoreLocation) {
                LongPressCopyableText(
                    context,
                    when {
                        playlist.isLocal -> context.symphony.t.LocalStorage
                        else -> context.symphony.t.AppBuiltIn
                    }
                )
            }
            playlist.path?.let {
                InformationKeyValue(context.symphony.t.Path, mono = true) {
                    LongPressCopyableText(context, it)
                }
            }
            InformationKeyValue(context.symphony.t.Id, mono = true) {
                LongPressCopyableText(context, playlist.id)
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
