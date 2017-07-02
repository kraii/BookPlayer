package com.github.kraii.bookplayer

import android.content.Context
import android.os.Environment
import org.jetbrains.anko.AnkoContext
import java.io.File

fun buildLibrary(): Library {
    val audioBooksDirectory = Environment.getExternalStorageDirectory().path + "/AudioBooks"
    return Library(File(audioBooksDirectory))
}

fun emptyLibrary(): Library {
    return Library()
}

object LibraryHolder {
    private val repository : LibraryRepository = LibraryRepository()
    private var library: Library = emptyLibrary()

    fun save(context: Context) {
        repository.save(context, library)
    }

    fun load(context: Context) {
        library = repository.load(context)
    }

    fun get(): Library {
        return library
    }

    fun updateFrom(newlyScanned: Library) {
        newlyScanned.mergeWith(library)
        library = newlyScanned
    }
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

    fun selectNextChapter(): Chapter? = selectedTitle?.nextChapter()


    fun selectedTitle() : Book? {
        return selectedTitle
    }

    fun currentlySelected(): Chapter? {
        return selectedTitle?.currentChapter()
    }

    fun updateTimestamp(nowPlayingTimestamp: Int) {
        selectedTitle?.currentChapterTimestamp = nowPlayingTimestamp
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

    fun findMatching(book: Book) : Book? = books.find { it.author == book.author && it.title == book.title }


    fun mergeWith(existing: Library) {
        books.forEach { book ->
            val existingBook = existing.findMatching(book)
            existingBook?.let {
                book.currentChapter = it.currentChapter
                book.currentChapterTimestamp = it.currentChapterTimestamp
            }
        }
        selectedTitle = existing.selectedTitle()
    }

}