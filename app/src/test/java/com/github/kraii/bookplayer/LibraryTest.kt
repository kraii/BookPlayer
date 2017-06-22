package com.github.kraii.bookplayer

import org.junit.Assert.*
import org.junit.Test

import java.io.File

class LibraryTest {
    @Test
    fun buildsLibrary() {
        val tempDir = createTempDir()
        val animalFarm = File(tempDir, "Animal_Farm-George_Orwell")
        animalFarm.mkdirs()
        for (i in 1..5) File(animalFarm, "$i.mp3").createNewFile()
        val library = Library(tempDir)
        tempDir.deleteRecursively()

        assertEquals(1, library.books.size)
        val book = library.books.first()
        assertEquals("George Orwell", book.author)
        assertEquals("Animal Farm", book.title)
        assertEquals(5, book.chapters.size)
    }
}