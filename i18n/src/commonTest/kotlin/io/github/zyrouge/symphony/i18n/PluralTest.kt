package io.github.zyrouge.symphony.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

class PluralTest {
    @Test
    fun `english picks one only for a single item`() {
        assertEquals("1 song", pluralTemplate("en", "1", one = "1 song", other = "8 songs").format("1"))
        assertEquals("8 songs", pluralTemplate("en", "8", one = "1 song", other = "8 songs").format("8"))
        assertEquals("0 songs", pluralTemplate("en", "0", one = "1 song", other = "0 songs").format("0"))
    }

    @Test
    fun `russian uses few and many bands`() {
        fun pick(n: Int) = pluralTemplate("ru", n.toString(), one = "one", few = "few", many = "many", other = "other")
        assertEquals("one", pick(1))
        assertEquals("few", pick(3))
        assertEquals("many", pick(5))
        assertEquals("many", pick(11))
        assertEquals("one", pick(21))
        assertEquals("few", pick(22))
    }

    @Test
    fun `polish and chinese follow their own rules`() {
        assertEquals("few", pluralTemplate("pl", "3", one = "one", few = "few", many = "many", other = "other"))
        assertEquals("many", pluralTemplate("pl", "12", one = "one", few = "few", many = "many", other = "other"))
        assertEquals("other", pluralTemplate("zh", "1", one = "one", few = "few", many = "many", other = "other"))
    }

    @Test
    fun `a missing band falls back to other`() {
        assertEquals("other", pluralTemplate("ru", "3", one = "one", other = "other"))
    }

    @Test
    fun `folders and files pick the folder count's form`() {
        val strings = Strings(
            values = mapOf(
                "XFoldersYFiles_one" to "%1\$s folder, %2\$s files",
                "XFoldersYFiles_other" to "%1\$s folders, %2\$s files",
            ),
            language = "en",
        )
        assertEquals("1 folder, 0 files", strings.XFoldersYFiles("1", "0"))
        assertEquals("3 folders, 1 files", strings.XFoldersYFiles("3", "1"))
    }

}
