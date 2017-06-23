package com.github.kraii.bookplayer

import android.os.Environment
import java.io.File

fun buildLibrary(): Library {
    val audioBooksDirectory = Environment.getExternalStorageDirectory().path + "/AudioBooks"
    return Library(File(audioBooksDirectory))
}

fun emptyLibrary() : Library {
    return Library()
}

class Library  {
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
        val walk = rootOfLibrary.walk()
                .onLeave { file ->
                    if (chapters.isNotEmpty()) {
                        val (author, title) = parseDirName(file)
                        bookBuilder.add(Book(author, title, chapters.toList().sortedBy { it.file.name }))
                        chapters = mutableListOf()
                    }
                }
        for (file in walk) {
            if (file.isFile && file.name.endsWith(".mp3", true)) {
                chapters.add(Chapter(file))
            }
        }
        books = bookBuilder.toList()
        selectedTitle = books.firstOrNull()
    }

    fun selectNextChapter() : Chapter? {
        if(selectedTitle == null) return null
        val book = selectedTitle!!
        return book.nextChapter()
    }

    fun currentlySelected() : Chapter? {
        return selectedTitle?.currentChapter()
    }
}