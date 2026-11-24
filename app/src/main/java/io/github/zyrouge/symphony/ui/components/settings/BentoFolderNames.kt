package io.github.zyrouge.symphony.ui.components.settings

import android.net.Uri
import android.provider.DocumentsContract

/**
 * A storage path as people read it. Scan paths and library folders start
 * with the volume id of their document tree ("primary:Music/Live"), so
 * "primary" becomes "Internal storage", other volumes are named by id, and
 * the folders are joined with " › ".
 */
fun bentoReadableFolderPath(
    raw: String,
    internalStorage: String = "Internal storage",
    storageX: (String) -> String = { "Storage $it" },
): String {
    val colon = raw.indexOf(':')
    val (volume, rest) = when {
        colon < 0 -> null to raw
        else -> raw.substring(0, colon) to raw.substring(colon + 1)
    }
    val parts = rest.split('/').filter { it.isNotEmpty() }
    val volumeName = when (volume) {
        null -> null
        "primary" -> internalStorage
        else -> storageX(volume)
    }
    return (listOfNotNull(volumeName) + parts).joinToString(" › ").ifEmpty { raw }
}

/** [bentoReadableFolderPath] of a media folder's tree URI, or its raw path if it is not one. */
fun bentoReadableTreeUri(
    uri: Uri,
    internalStorage: String = "Internal storage",
    storageX: (String) -> String = { "Storage $it" },
): String =
    runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        ?.let { bentoReadableFolderPath(it, internalStorage, storageX) }
        ?: Uri.decode(uri.path ?: uri.toString())
