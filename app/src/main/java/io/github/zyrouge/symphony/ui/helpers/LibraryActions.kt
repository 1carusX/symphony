package io.github.zyrouge.symphony.ui.helpers

import io.github.zyrouge.symphony.services.groove.Groove

fun libraryRescanAction(context: ViewContext): () -> Unit = {
    context.symphony.radio.stop()
    context.symphony.groove.fetch(
        Groove.FetchOptions(resetInMemoryCache = true),
    )
}
