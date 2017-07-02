package com.github.kraii.bookplayer

import android.os.Environment
import java.io.File

fun buildLibrary(): Library {
    val audioBooksDirectory = Environment.getExternalStorageDirectory().path + "/AudioBooks"
    return Library(File(audioBooksDirectory))
}

fun emptyLibrary(): Library {
    return Library()
}

class Library {
    val books: List<Book>
    private var selectedTitle: Book?

    private fun parseDirName(file: File): Pair<String, String> {
        val split: List<String> = file.name.replace("_", " ").split("-")
        val title = split.firstOrNull() ?: ""
        val author = if (split.size > 1) split[1] else ""
        return Pair(author, title)
    }

    override fun toString(): String {
        return "Library(books=$books)"
    }

    internal constructor() {
        books = emptyList()
        selectedTitle = null
    }

    internal constructor(rootOfLibrary: File) {
        val bookBuilder: MutableList<Book> = mutableListOf()
        var chapters: MutableList<Chapter> = mutableListOf()
        var cover: File? = null
        val walk = rootOfLibrary.walk()
                .onLeave { file ->
                    if (chapters.isNotEmpty()) {
                        val (author, title) = parseDirName(file)
                        val sortedChapters = chapters.toList().sortedBy { it.file.name }
                        bookBuilder.add(Book(author, title, sortedChapters, cover))
                        chapters = mutableListOf()
                        cover = null
                    }
                }
        for (file in walk) {
            if (file.isFile && file.name.endsWith(".mp3", true)) {
                chapters.add(Chapter(file))
            } else if(file.isFile && file.name == "cover.jpg") {
                cover = file
            }
        }
        books = bookBuilder.toList()
        selectedTitle = books.firstOrNull()
    }

    fun selectNextChapter(): Chapter? {
        if (selectedTitle == null) return null
        val book = selectedTitle!!
        return book.nextChapter()
    }

    fun currentlySelected(): Chapter? {
        return selectedTitle?.currentChapter()
    }

    fun updateTimestamp(nowPlayingTimestamp: Int) {
        selectedTitle?.setCurrentChapterTimestamp(nowPlayingTimestamp)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Library

        if (books != other.books) return false
        if (selectedTitle != other.selectedTitle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = books.hashCode()
        result = 31 * result + (selectedTitle?.hashCode() ?: 0)
        return result
    }

}