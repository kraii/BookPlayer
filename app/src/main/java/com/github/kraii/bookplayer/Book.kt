package com.github.kraii.bookplayer

import java.io.File

data class Book(val author: String, val title: String, val chapters: List<Chapter>, var coverPath : String? = null)

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