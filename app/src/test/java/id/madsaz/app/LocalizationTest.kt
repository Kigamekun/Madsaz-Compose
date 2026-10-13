package id.madsaz.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Menjaga tiga bahasa tetap sinkron: kunci sama, bentuk jamak lengkap, placeholder sama, dan tak ada string yatim. */
class LocalizationTest {
    private val res = File("src/main/res")

    /** [placeholders] untuk string biasa; untuk plurals berisi placeholder bentuk `other` (bentuk lain boleh tanpa angka, mis. "تسجيل واحد"). */
    private class Entry(val quantities: Set<String>, val placeholders: Map<String, Int>, val byQuantity: Map<String, Map<String, Int>> = emptyMap())

    private fun load(folder: String): Map<String, Entry> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(res, "$folder/strings.xml"))
        val entries = mutableMapOf<String, Entry>()
        val nodes = document.documentElement.childNodes
        for (index in 0 until nodes.length) {
            val node = nodes.item(index) as? Element ?: continue
            val name = node.getAttribute("name")
            when (node.tagName) {
                "string" -> entries["string/$name"] = Entry(emptySet(), placeholders(node.textContent))
                "plurals" -> {
                    val items = node.getElementsByTagName("item")
                    val quantities = (0 until items.length).map { (items.item(it) as Element).getAttribute("quantity") }.toSet()
                    val byQuantity = (0 until items.length).associate {
                        val item = items.item(it) as Element
                        item.getAttribute("quantity") to placeholders(item.textContent)
                    }
                    entries["plurals/$name"] = Entry(quantities, byQuantity.getValue("other"), byQuantity)
                }
            }
        }
        return entries
    }

    private fun placeholders(text: String): Map<String, Int> =
        Regex("%(\\d+\\$)?[sd]").findAll(text).map { it.value.replace(Regex("^%\\d+\\$"), "%") }.groupingBy { it }.eachCount()

    private val defaults = load("values")
    private val english = load("values-en")
    private val arabic = load("values-ar")

    // Kunci translatable=false hanya boleh ada di default.
    private val untranslatable: Set<String> = run {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(res, "values/strings.xml"))
        val strings = document.getElementsByTagName("string")
        (0 until strings.length).map { strings.item(it) as Element }
            .filter { it.getAttribute("translatable") == "false" }.map { "string/" + it.getAttribute("name") }.toSet()
    }

    private val translatable get() = defaults.keys - untranslatable

    @Test
    fun `English and Arabic define exactly the translatable keys of the default Indonesian file`() {
        assertEquals("kunci en yang kurang/berlebih", emptySet<String>(), translatable.symmetricDifference(english.keys))
        assertEquals("kunci ar yang kurang/berlebih", emptySet<String>(), translatable.symmetricDifference(arabic.keys))
    }

    @Test
    fun `plural forms follow each language rules`() {
        val plurals = translatable.filter { it.startsWith("plurals/") }
        assertTrue(plurals.isNotEmpty())
        for (key in plurals) {
            assertEquals("id $key", setOf("other"), defaults.getValue(key).quantities)
            assertEquals("en $key", setOf("one", "other"), english.getValue(key).quantities)
            assertEquals("ar $key", setOf("zero", "one", "two", "few", "many", "other"), arabic.getValue(key).quantities)
        }
    }

    @Test
    fun `format placeholders are identical in every language`() {
        for (key in translatable) {
            val expected = defaults.getValue(key).placeholders
            assertEquals("en $key", expected, english.getValue(key).placeholders)
            assertEquals("ar $key", expected, arabic.getValue(key).placeholders)
        }
        // Bentuk jamak yang memuat angka (few/many) harus memakai placeholder yang sama dengan `other`.
        for (key in translatable.filter { it.startsWith("plurals/") }) {
            val other = arabic.getValue(key).placeholders
            for (quantity in listOf("few", "many")) assertEquals("ar $key/$quantity", other, arabic.getValue(key).byQuantity.getValue(quantity))
            assertEquals("en $key/one", english.getValue(key).byQuantity.getValue("other"), english.getValue(key).byQuantity.getValue("one"))
        }
    }

    @Test
    fun `every string is referenced from code, the manifest or resources`() {
        val sources = File("src/main").walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.name == "AndroidManifest.xml" || (it.extension == "xml" && it.path.contains("/res/") && !it.name.startsWith("strings"))) }
            .joinToString("\n") { it.readText() }
        val unused = defaults.keys.filter { key ->
            val (kind, name) = key.split("/")
            val rKind = if (kind == "string") "string" else "plurals"
            !sources.contains("R.$rKind.$name") && !sources.contains("@$rKind/$name")
        }
        assertEquals("string yatim (hapus dari ketiga bahasa)", emptyList<String>(), unused)
    }

    private fun <T> Set<T>.symmetricDifference(other: Set<T>): Set<T> = (this - other) + (other - this)
}
