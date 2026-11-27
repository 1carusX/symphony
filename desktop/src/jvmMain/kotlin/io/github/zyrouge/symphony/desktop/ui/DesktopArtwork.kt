package io.github.zyrouge.symphony.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jetbrains.skia.Image as SkiaImage

private val artworkCache = ConcurrentHashMap<Path, ImageBitmap>()
private val missingArtwork = ConcurrentHashMap.newKeySet<Path>()

@Composable
fun DesktopArtwork(path: Path, modifier: Modifier) {
    val bitmap by produceState(initialValue = artworkCache[path], path) {
        if (value != null || path in missingArtwork) return@produceState
        val loaded = withContext(Dispatchers.IO) { loadArtwork(path) }
        if (loaded != null) {
            artworkCache[path] = loaded
        } else {
            missingArtwork.add(path)
        }
        value = loaded
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer))
    }
}

internal fun loadArtwork(path: Path): ImageBitmap? = runCatching {
    val bytes = AudioFileIO.read(path.toFile())
        .tag
        ?.firstArtwork
        ?.binaryData
        ?: return null
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}.getOrNull()
