package com.github.kraii.bookplayer

import java.io.File

data class AuthorTitle(val author: String, val title: String)

data class Book(
        val authorTitle: AuthorTitle,
        val chapters: List<Chapter>,
        var cover: File? = null,
        var currentChapter: Int = 0
) {

    fun firstChapter(): Chapter = chapters.first()
    fun currentChapter(): Chapter = chapters[currentChapter]

    fun nextChapter(): Chapter? {
        assert(currentChapter >= 0)
        assert(currentChapter <= chapters.lastIndex)
        if (currentChapter >= chapters.lastIndex)
            return null
        else {
            currentChapter++
            return chapters[currentChapter]
        }
    }

    var currentChapterTimestamp: Int
        get() = currentChapter().currentTimestamp
        set(value) {
            currentChapter().currentTimestamp = value
        }

    val author: String
        get() = authorTitle.author

    val title: String
        get() = authorTitle.title
}

data class Chapter(val file: File, var currentTimestamp: Int = 0)