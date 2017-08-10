package com.github.kraii.bookplayer

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LibraryTest {
    val tempDir = createTempDir()
    val library = buildLibrary("George_Orwell-Animal_Farm")


    @Test
    fun buildsLibrary() {
        assertEquals(1, library.books.size)
        val book = library.books.first()
        assertEquals("George Orwell", book.author)
        assertEquals("Animal Farm", book.title)
        assertEquals(5, book.chapters.size)
        assertEquals(File(tempDir, "George_Orwell-Animal_Farm/cover.jpg"), book.cover)
    }

    @Test
    fun mergesLibraryRetainingBookProgess() {
        library.selectNextChapter()
        library.updateTimestamp(2000)
        // note the animal farm files will still exist when this is called
        val newlyScannedLibrary = buildLibrary("George_Orwell-1984", 10)
        newlyScannedLibrary.mergeWith(library)


        assertEquals(2, newlyScannedLibrary.books.size)
        val selectedTitle: Book? = newlyScannedLibrary.selectedTitle()
        assertEquals("Animal Farm", selectedTitle?.title)
        assertEquals(1, selectedTitle?.currentChapter)
        assertEquals(2000, selectedTitle?.currentChapterTimestamp)
    }

    @Test
    fun retainsSelectedTitleUponMerge() {
        val libraryWith2Books = Library(listOf(
                Book(AuthorTitle("Eric", "Animal Farm"), listOf(Chapter(File("Start")), Chapter(File("Pigs")))),
                Book(AuthorTitle("J.K Rowling", "Harry Potter and the Goblet of Fire"), listOf(Chapter(File("Wizards"))))
        ))
        val refreshedLibrary = Library(listOf(
                Book(AuthorTitle("Eric", "Animal Farm"), listOf(Chapter(File("Start")), Chapter(File("Pigs")))),
                Book(AuthorTitle("J.K Rowling", "Harry Potter and the Goblet of Fire"), listOf(Chapter(File("Wizards"))))
        ))

        libraryWith2Books.selectNextTitle()
        libraryWith2Books.mergeWith(refreshedLibrary)
        assertEquals("Harry Potter and the Goblet of Fire", libraryWith2Books.selectedTitle()?.title)
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
        val libraryWith2Books = Library(listOf(
                Book(AuthorTitle("Eric", "Animal Farm"), listOf(Chapter(File("Start")), Chapter(File("Pigs")))),
                Book(AuthorTitle("Eric", "1984"), listOf(Chapter(File("Start"))))
        ))
        libraryWith2Books.selectNextChapter()

        libraryWith2Books.selectNextTitle()
        assertEquals("1984", libraryWith2Books.selectedTitle()?.title)
        assertEquals("Start", nameOf(libraryWith2Books.currentlySelected()))

        libraryWith2Books.selectNextTitle()
        assertEquals("Animal Farm", libraryWith2Books.selectedTitle()?.title)
        assertEquals("should still have the same chapter selected", "Pigs", nameOf(libraryWith2Books.currentlySelected()))
    }

    @Test
    fun selectsPreviousTitle() {
        val libraryWith2Books = Library(listOf(
                Book(AuthorTitle("Eric", "Animal Farm"), listOf(Chapter(File("Start")), Chapter(File("Pigs")))),
                Book(AuthorTitle("J.K Rowling", "Harry Potter and the Goblet of Fire"), listOf(Chapter(File("Wizards"))))
        ))
        libraryWith2Books.selectNextChapter()

        libraryWith2Books.selectPreviousTitle()
        assertEquals("Harry", libraryWith2Books.selectedTitle()?.title?.subSequence(0..4))
        assertEquals("Wizards", nameOf(libraryWith2Books.currentlySelected()))

        libraryWith2Books.selectPreviousTitle()
        assertEquals("Animal Farm", libraryWith2Books.selectedTitle()?.title)
        assertEquals("should still have the same chapter selected", "Pigs", nameOf(libraryWith2Books.currentlySelected()))
    }

    @Test
    fun libraryAsJson() {
        library.selectNextChapter()
        library.updateTimestamp(10)
        val libraryRepository = LibraryRepository()
        val json = libraryRepository.toJson(library)
        val loadedLibrary = libraryRepository.fromJson(json)
        assertEquals(library.books, loadedLibrary.books)
        assertEquals(library.selectedTitle(), loadedLibrary.selectedTitle())
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
