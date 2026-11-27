package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Immutable

/**
 * The letter an entry files under (DESIGN.md 5.2): the first character of its
 * sort key, uppercased; anything outside A-Z files under "#".
 */
fun resolveBentoLetter(sortKeyValue: String): String {
    val c = sortKeyValue.firstOrNull()?.uppercaseChar() ?: return "#"
    return if (c in 'A'..'Z') c.toString() else "#"
}

/**
 * One run of consecutive entries sharing a letter (DESIGN.md 5.2). A null
 * letter means the sort shows a plain list.
 */
@Immutable
data class BentoLetterGroup<T>(val letter: String?, val entries: List<T>)

/**
 * Groups already-ordered entries into consecutive letter runs. Only lettered
 * sorts group; the others return a single plain group.
 */
fun <T> groupBentoEntriesByLetter(
    entries: List<T>,
    lettered: Boolean,
    sortKeyOf: (T) -> String,
): List<BentoLetterGroup<T>> {
    if (!lettered) {
        return listOf(BentoLetterGroup(letter = null, entries = entries))
    }
    val groups = mutableListOf<Pair<String, MutableList<T>>>()
    entries.forEach { entry ->
        val letter = resolveBentoLetter(sortKeyOf(entry))
        val last = groups.lastOrNull()
        if (last != null && last.first == letter) {
            last.second.add(entry)
        } else {
            groups.add(letter to mutableListOf(entry))
        }
    }
    return groups.map { BentoLetterGroup(letter = it.first, entries = it.second) }
}
