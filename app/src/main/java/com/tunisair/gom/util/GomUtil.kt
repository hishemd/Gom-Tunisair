package com.tunisair.gom.util

import com.tunisair.gom.data.model.Bookmark

/**
 * Weight and measures conversions transcribed from GOM 0.8.
 * Each entry multiplies the source value by [factor].
 */
data class ConversionRow(
    val from: String,
    val to: String,
    val factor: Double,
    val family: Family
) {
    enum class Family { Weight, Length, Volume, Area, Temperature }
}

object Conversions {

    val rows: List<ConversionRow> = listOf(
        ConversionRow("Kilograms", "Pounds", 2.2046, ConversionRow.Family.Weight),
        ConversionRow("Pounds", "Kilograms", 0.4536, ConversionRow.Family.Weight),
        ConversionRow("Pounds", "Ounces", 16.0, ConversionRow.Family.Weight),
        ConversionRow("Ounces", "Grams", 28.3495, ConversionRow.Family.Weight),
        ConversionRow("Grams", "Ounces", 0.0353, ConversionRow.Family.Weight),
        ConversionRow("Tons (metric)", "Pounds", 2205.0, ConversionRow.Family.Weight),
        ConversionRow("Tons (short)", "Kilograms", 907.1849, ConversionRow.Family.Weight),
        ConversionRow("Tons (short)", "Tons (metric)", 0.9072, ConversionRow.Family.Weight),
        ConversionRow("Tons (long)", "Kilograms", 1016.0, ConversionRow.Family.Weight),
        ConversionRow("Tons (long)", "Tons (short)", 1.12, ConversionRow.Family.Weight),

        ConversionRow("Centimetres", "Inches", 0.3937, ConversionRow.Family.Length),
        ConversionRow("Inches", "Centimetres", 2.54, ConversionRow.Family.Length),
        ConversionRow("Metres", "Feet", 3.2808, ConversionRow.Family.Length),
        ConversionRow("Feet", "Metres", 0.3048, ConversionRow.Family.Length),
        ConversionRow("Feet", "Inches", 12.0, ConversionRow.Family.Length),
        ConversionRow("Millimetres", "Inches", 0.0394, ConversionRow.Family.Length),
        ConversionRow("Metres", "Inches", 39.37, ConversionRow.Family.Length),

        ConversionRow("Cubic metres", "Cubic feet", 35.31, ConversionRow.Family.Volume),
        ConversionRow("Cubic feet", "Litres", 28.32, ConversionRow.Family.Volume),
        ConversionRow("Cubic feet", "Cubic inches", 1728.0, ConversionRow.Family.Volume),
        ConversionRow("Cubic inches", "Cubic centimetres", 16.39, ConversionRow.Family.Volume),
        ConversionRow("Litres", "U.S. gallons", 0.2642, ConversionRow.Family.Volume),
        ConversionRow("Litres", "Imperial gallons", 0.22, ConversionRow.Family.Volume),
        ConversionRow("U.S. gallons", "Litres", 3.7853, ConversionRow.Family.Volume),
        ConversionRow("Imperial gallons", "Litres", 4.546, ConversionRow.Family.Volume),
        ConversionRow("Litres", "Imperial pints", 1.7598, ConversionRow.Family.Volume),

        ConversionRow("Kilograms / cubic metre", "Pounds / cubic foot", 0.0624, ConversionRow.Family.Weight),
        ConversionRow("Pounds / cubic foot", "Kilograms / cubic metre", 16.0166, ConversionRow.Family.Weight),
        ConversionRow("Kilograms per square metre", "Pounds per square foot", 0.205, ConversionRow.Family.Weight),

        ConversionRow("Square inches", "Square centimetres", 6.451589, ConversionRow.Family.Area),
        ConversionRow("Square centimetres", "Square inches", 0.155001, ConversionRow.Family.Area),

        ConversionRow("in/lb (Burst)", "Beach units", 0.265, ConversionRow.Family.Weight),
        ConversionRow("in/oz per in of tear", "in/lb", 0.28, ConversionRow.Family.Weight)
    )

    /** Celsius to Fahrenheit and back, the two entries of GOM 0.8 that are not a simple factor. */
    fun celsiusToFahrenheit(c: Double) = c * 9 / 5 + 32

    fun fahrenheitToCelsius(f: Double) = (f - 32) * 5 / 9

    fun convert(from: String, to: String, value: Double): Double? =
        rows.firstOrNull { it.from.equals(from, true) && it.to.equals(to, true) }?.factor?.let { value * it }

    /** Pounds to kilograms, the figure most often needed on the ramp. */
    fun poundsToKg(lb: Double) = lb * 0.4536

    fun kgToPounds(kg: Double) = kg * 2.2046
}

/**
 * Bookmarks held as JSON in the app's internal files directory.
 *
 * Deliberately dependency free: the manual has no third party requirements and the
 * store only has to round-trip a small list.
 */
class BookmarkStore(private val file: java.io.File) {

    @Synchronized
    fun load(): List<Bookmark> {
        if (!file.exists()) return emptyList()
        val text = runCatching { file.readText() }.getOrDefault("")
        val items = text.trim().split("\n").filter { it.isNotBlank() }
        return items.mapNotNull { decode(it) }
    }

    @Synchronized
    fun save(items: List<Bookmark>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(items.joinToString("\n") { encode(it) })
        }
    }

    private fun encode(b: Bookmark): String = listOf(
        b.chapterId.toString(),
        b.sectionId,
        b.title,
        b.excerpt,
        b.timestamp.toString()
    ).joinToString("\u0001") { it.replace('\n', ' ') }

    private fun decode(line: String): Bookmark? {
        val parts = line.split("\u0001")
        if (parts.size < 4) return null
        return Bookmark(
            chapterId = parts[0].toIntOrNull() ?: return null,
            sectionId = parts[1],
            title = parts[2],
            excerpt = parts[3],
            timestamp = parts.getOrNull(4)?.toLongOrNull() ?: 0L
        )
    }
}
