package com.tunisair.gom.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tunisair.gom.data.GomContent
import com.tunisair.gom.data.model.Bookmark
import com.tunisair.gom.util.BookmarkStore

/** Top level destinations of the app. */
enum class Destination(val label: String) {
    Home("Home"),
    Chapters("Chapters"),
    Search("Search"),
    Bookmarks("Bookmarks"),
    Calculator("Calculator")
}

/**
 * Application state shared by the screens.
 *
 * Search, bookmarks and the open section are kept here so that a configuration
 * change or a back navigation from a section returns to the same place.
 */
class GomViewModel(store: BookmarkStore) {

    var destination: Destination by mutableStateOf(Destination.Home)
        private set

    var query: String by mutableStateOf("")
        private set

    /** Chapter id currently open, or null when the chapter list is shown. */
    var openChapter: Int? by mutableStateOf(null)
        private set

    /** Section id expanded inside the open chapter. */
    var openSection: String? by mutableStateOf(null)
        private set

    var bookmarks: List<Bookmark> by mutableStateOf(store.load())
        private set

    val results: List<GomContent.Hit>
        get() = GomContent.search(query)

    private val store = store

    fun goTo(destination: Destination) {
        this.destination = destination
        if (destination != Destination.Chapters) {
            openChapter = null
            openSection = null
        }
    }

    fun setQuery(value: String) {
        query = value
    }

    fun openChapter(chapterId: Int, sectionId: String? = null) {
        openChapter = chapterId
        openSection = sectionId
        destination = Destination.Chapters
    }

    fun toggleSection(sectionId: String) {
        openSection = if (openSection == sectionId) null else sectionId
    }

    /** Steps back one level: section, then chapter, then the previous tab. */
    fun back(): Boolean = when {
        openSection != null -> {
            openSection = null
            true
        }
        openChapter != null -> {
            openChapter = null
            true
        }
        destination != Destination.Home -> {
            destination = Destination.Home
            true
        }
        else -> false
    }

    fun isBookmarked(chapterId: Int, sectionId: String) =
        bookmarks.any { bm: Bookmark -> bm.chapterId == chapterId && bm.sectionId == sectionId }

    fun toggleBookmark(chapterId: Int, sectionId: String, title: String, excerpt: String) {
        val existing = bookmarks.firstOrNull { bm: Bookmark -> bm.chapterId == chapterId && bm.sectionId == sectionId }
        bookmarks = if (existing != null) {
            bookmarks - existing
        } else {
            bookmarks + Bookmark(chapterId, sectionId, title, excerpt)
        }
        store.save(bookmarks)
    }

    fun removeBookmark(chapterId: Int, sectionId: String) {
        bookmarks = bookmarks.filterNot { bm: Bookmark -> bm.chapterId == chapterId && bm.sectionId == sectionId }
        store.save(bookmarks)
    }

    fun clearBookmarks() {
        bookmarks = emptyList()
        store.save(bookmarks)
    }

    fun excerptOf(chapterId: Int, sectionId: String): String {
        val chapter = GomContent.chapterById(chapterId) ?: return ""
        val section = chapter.sections.firstOrNull { sec: GomContent.Section -> sec.id == sectionId } ?: return ""
        return section.content.replace('\n', ' ').trim().take(160)
    }
}
