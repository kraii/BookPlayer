package com.github.kraii.bookplayer

import java.io.File

data class Book(
        val author: String,
        val title: String,
        val chapters: List<Chapter>,
        var coverPath: String? = null
) {
    private var currentChapter: Int = 0

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

    fun setCurrentChapterTimestamp(timestamp: Int) {
        currentChapter().currentTimestamp = timestamp
    }
}

data class Chapter(val file: File, var currentTimestamp: Int = 0)