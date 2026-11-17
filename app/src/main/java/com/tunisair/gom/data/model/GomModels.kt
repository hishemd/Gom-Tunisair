package com.tunisair.gom.data.model

data class Chapter(
    val id: Int,
    val number: String,
    val title: String,
    val titleArabic: String,
    val sections: List<Section>,
    val color: Long
)

data class Section(
    val id: String,
    val title: String,
    val content: String,
    val subsections: List<Subsection> = emptyList()
)

data class Subsection(
    val id: String,
    val title: String,
    val content: String
)

data class Bookmark(
    val chapterId: Int,
    val sectionId: String,
    val title: String,
    val excerpt: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConversionResult(
    val fromUnit: String,
    val toUnit: String,
    val fromValue: Double,
    val toValue: Double,
    val formula: String
)
