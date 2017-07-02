package com.github.kraii.bookplayer

import android.content.Context
import android.os.Environment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File

fun buildLibrary(): Library {
    val audioBooksDirectory = Environment.getExternalStorageDirectory().path + "/AudioBooks"
    return Library(File(audioBooksDirectory))
}

fun emptyLibrary(): Library {
    return Library()
}

object LibraryHolder : AnkoLogger {
    private val repository : LibraryRepository = LibraryRepository()
    private var library: Library = emptyLibrary()

    fun save(context: Context) {
        repository.save(context, library)
        info("Saved $library")
    }

    fun load(context: Context) {
        library = repository.load(context)
        info("Loaded $library")
    }

    fun get(): Library {
        return library
    }

    fun updateFrom(newlyScanned: Library) {
//        newlyScanned.mergeWith(library)
        library = newlyScanned
    }
}

class Library : AnkoLogger {
    val books: List<Book>
    private var selectedTitle: Book?

    private fun parseDirName(file: File): AuthorTitle {
        val split: List<String> = file.name.replace("_", " ").split("-")
        val title = split.firstOrNull() ?: ""
        val author = if (split.size > 1) split[1] else ""
        return AuthorTitle(author, title)
    }

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

    internal constructor(rootOfLibrary: File) {
        val bookBuilder: MutableList<Book> = mutableListOf()
        var chapters: MutableList<Chapter> = mutableListOf()
        var cover: File? = null
        val walk = rootOfLibrary.walk()
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
            info("Walking through file $file")
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
}