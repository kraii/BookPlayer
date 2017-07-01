package com.github.kraii.bookplayer

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LibraryTest {
    val library = buildLibrary()

    @Test
    fun buildsLibrary() {
        assertEquals(1, library.books.size)
        val book = library.books.first()
        assertEquals("George Orwell", book.author)
        assertEquals("Animal Farm", book.title)
        assertEquals(5, book.chapters.size)
    }

    @Test
    fun selectsNextChapter() {
        assertEquals("1.mp3", nameOf(library.currentlySelected()))
        assertEquals("2.mp3", nameOf(library.selectNextChapter()))
        assertEquals("2.mp3", nameOf(library.currentlySelected()))

        library.selectNextChapter()
        library.selectNextChapter()
        library.selectNextChapter()
        assertEquals(null, library.selectNextChapter())
        assertEquals("5.mp3", nameOf(library.currentlySelected()))
    }

    @Test
    fun libraryAsJson() {
        library.selectNextChapter()
        library.updateTimestamp(10)

        val libraryRepository = LibraryRepository()
        val json = libraryRepository.toJson(library)
        val loadedLibrary = libraryRepository.fromJson(json)
        assertEquals(library, loadedLibrary)
    }

    private fun nameOf(chapter: Chapter?): String {
        return chapter?.file?.name ?: "Nothing Selected :("
    }

    private fun buildLibrary(): Library {
        val tempDir = createTempDir()
        val animalFarm = File(tempDir, "Animal_Farm-George_Orwell")
        animalFarm.mkdirs()
        for (i in 1..5) File(animalFarm, "$i.mp3").createNewFile()
        val library = Library(tempDir)
        tempDir.deleteRecursively()
        return library
    }
}