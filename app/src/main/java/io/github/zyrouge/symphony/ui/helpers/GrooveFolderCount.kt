package io.github.zyrouge.symphony.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.utils.SimpleFileSystem

/**
 * Number of folders that directly contain songs, from the current song
 * explorer tree. Recomputed when the scanned song set changes.
 */
@Composable
fun rememberGrooveFolderCount(context: ViewContext): Int {
    val id by context.symphony.groove.song.id.collectAsState()
    val explorer = context.symphony.groove.song.explorer
    return remember(id) {
        var count = 0
        val stack = ArrayDeque<SimpleFileSystem.Folder>()
        stack.addLast(explorer)
        while (stack.isNotEmpty()) {
            val current = stack.removeLast()
            if (current.isEmpty) continue
            var hasSongs = false
            current.children.values.forEach {
                when (it) {
                    is SimpleFileSystem.Folder -> stack.addLast(it)
                    is SimpleFileSystem.File -> hasSongs = true
                }
            }
            if (hasSongs) count++
        }
        count
    }
}
