package com.github.kraii.bookplayer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LibraryTest {
    val tempDir = createTempDir()
    val library = buildLibrary("Animal_Farm-George_Orwell")

    @Test
    fun buildsLibrary() {
        assertEquals(1, library.books.size)
        val book = library.books.first()
        assertEquals("George Orwell", book.author)
        assertEquals("Animal Farm", book.title)
        assertEquals(5, book.chapters.size)
        assertEquals(File(tempDir, "Animal_Farm-George_Orwell/cover.jpg"), book.cover)
    }

    @Test
    fun mergesLibraryRetainingSelectionsAndTimestamps() {
        library.selectNextChapter()
        library.updateTimestamp(2000)
        // note the animal farm files will still exist when this is called
        val newlyScannedLibrary = buildLibrary("George_Orwell-1984", 10)
        newlyScannedLibrary.mergeWith(library)

        assertEquals(2, newlyScannedLibrary.books.size)
        val selectedTitle : Book? = newlyScannedLibrary.selectedTitle()
        assertEquals("Animal Farm", selectedTitle?.title)
        assertEquals(1, selectedTitle?.currentChapter)
        assertEquals(2000, selectedTitle?.currentChapterTimestamp)
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
    fun selectsNextTitle() {
        val libraryWith2Books = buildLibrary("1984-George Orwell", 10)
        libraryWith2Books.selectNextChapter()

        libraryWith2Books.selectNextTitle()
        assertEquals("1984", libraryWith2Books.selectedTitle()?.title)
        assertEquals("1.mp3", nameOf(libraryWith2Books.currentlySelected()))

        libraryWith2Books.selectNextTitle()
        assertEquals("Animal Farm", libraryWith2Books.selectedTitle()?.title)
        assertEquals("should still have the same chapter selected", "2.mp3", nameOf(libraryWith2Books.currentlySelected()))
    }

    @Test
    fun selectsPreviousTitle() {
        val libraryWith2Books = buildLibrary("Harry Potter and the Goblet of Fire-J.K. Rowling", 2)
        libraryWith2Books.selectNextChapter()

        libraryWith2Books.selectPreviousTitle()
        assertEquals("Harry", libraryWith2Books.selectedTitle()?.title?.subSequence(0..4))
        assertEquals("1.mp3", nameOf(libraryWith2Books.currentlySelected()))

        libraryWith2Books.selectPreviousTitle()
        assertEquals("Animal Farm", libraryWith2Books.selectedTitle()?.title)
        assertEquals("should still have the same chapter selected", "2.mp3", nameOf(libraryWith2Books.currentlySelected()))
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

    private fun buildLibrary(bookName: String, highestNumberedBook: Int = 5): Library {
        val bookDir = File(tempDir, bookName)
        bookDir.mkdirs()
        for (i in 1..highestNumberedBook) File(bookDir, "$i.mp3").createNewFile()
        File(bookDir, "cover.jpg").createNewFile()
        val library = buildLibrary(tempDir)
        return library
    }

    @After
    fun afterTest() {
        tempDir.deleteRecursively()
    }
}