package com.github.kraii.bookplayer

import java.io.File

class Book(val author: String, val title: String, val chapters: List<Chapter>, var coverPath : String? = null) {
    private var currentChapter : Int = 0

    fun firstChapter() : Chapter {
        return chapters.first()
    }

    fun nextChapter(): Chapter? {
        assert(currentChapter >= 0)
        assert(currentChapter <= chapters.lastIndex)
        if(currentChapter >= chapters.lastIndex)
            return null
        else {
            currentChapter++
            return chapters[currentChapter]
        }
    }

    fun currentChapter(): Chapter {
        return chapters[currentChapter]
    }
}

class Chapter(val file: File) {
    override fun toString(): String {
        return "Chapter(file=$file)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Chapter

        if (file.name != other.file.name) return false

        return true
    }

    override fun hashCode(): Int {
        return file.name.hashCode()
    }


}