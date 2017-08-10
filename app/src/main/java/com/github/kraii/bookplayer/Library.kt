package com.github.kraii.bookplayer

import android.content.Context
import android.os.Environment
import java.io.File

fun buildLibrary() : Library {
    val audioBooksDirectory = Environment.getExternalStorageDirectory().path + "/AudioBooks"
    return buildLibrary(File(audioBooksDirectory))
}

fun buildLibrary(audioBooksDirectory: File): Library {
    val bookBuilder: MutableList<Book> = mutableListOf()
    var chapters: MutableList<Chapter> = mutableListOf()
    var cover: File? = null
    val walk = audioBooksDirectory.walk()
            .onLeave { file ->
                if (chapters.isNotEmpty()) {
                    val authorTitle = parseDirName(file)
                    val sortedChapters = chapters.toList().sortedBy { it.file.name }
                    bookBuilder.add(Book(authorTitle, sortedChapters, cover))
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
    return Library(bookBuilder.toList())
}

private fun parseDirName(file: File): AuthorTitle {
    val split: List<String> = file.name.replace("_", " ").split("-")
    val author = split.firstOrNull() ?: ""
    val title = if (split.size > 1) split[1] else ""
    return AuthorTitle(author, title)
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
        library = newlyScanned
    }
}

class Library {
    internal val books: List<Book>
    private var selectedTitle: Book?

    override fun toString(): String {
        return "Library(selectedTitle=$selectedTitle, books=$books)"
    }

    internal constructor() {
        books = emptyList()
        selectedTitle = null
    }

    internal constructor(books: List<Book>, selectedTitle: AuthorTitle?) {
        this.books = books
        this.selectedTitle = selectedTitle?.let { findMatching(it) }
    }

    internal constructor(books: List<Book>) {
        this.books = books
        this.selectedTitle = books.firstOrNull()
    }

    fun selectNextChapter(): Chapter? = selectedTitle?.nextChapter()
    fun selectPreviousChapter(): Chapter? = selectedTitle?.previousChapter()

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

    fun findMatching(book: AuthorTitle) : Book? = books.find { it.author == book.author && it.title == book.title }

    fun mergeWith(other: Library) {
        books.forEach { book ->
            val matchingBook = other.findMatching(book.authorTitle)
            matchingBook?.let {
                book.currentChapter = it.currentChapter
                book.currentChapterTimestamp = it.currentChapterTimestamp
            }
        }
        selectedTitle = other.selectedTitle()
    }

    fun selectNextTitle() {
        val currentSelectedTitle = selectedTitle
        if(currentSelectedTitle == null) {
            if(books.isNotEmpty()) selectedTitle = books.first()
        } else {
            val currentBookIndex = books.indexOf(currentSelectedTitle)
            if(currentBookIndex + 1 >= books.size) {
                // wrap around to start
                selectedTitle = books.first()
            } else {
                selectedTitle = books[currentBookIndex + 1]
            }
        }
    }

    fun selectPreviousTitle() {
        val currentSelectedTitle = selectedTitle
        if(currentSelectedTitle == null) {
            if(books.isNotEmpty()) selectedTitle = books.last()
        } else {
            val currentBookIndex = books.indexOf(currentSelectedTitle)
            if(currentBookIndex - 1 < 0) {
                // wrap around to end
                selectedTitle = books.last()
            } else {
                selectedTitle = books[currentBookIndex - 1]
            }
        }
    }
}
