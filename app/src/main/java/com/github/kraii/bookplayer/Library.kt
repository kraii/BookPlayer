package com.github.kraii.bookplayer

import android.content.Context
import java.io.File

fun buildLibrary(context: Context) {

}

class Library internal constructor(rootOfLibrary: File) {
    val books: List<Book>

    private fun parseDirName(file: File): Pair<String, String> {
        val split: List<String> = file.name.replace("_", " ").split("-")
        val title = split.firstOrNull() ?: ""
        val author = if (split.size > 1) split[1] else ""
        return Pair(author, title)
    }

    init {
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
    }


}