package com.github.kraii.bookplayer

import com.github.kittinunf.fuel.Fuel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GoogleBooksApiTest {

    @Before
    fun setUpFuel() {
        Fuel.testMode {
            timeout = 5000
        }
    }

    @Test
    fun searchForBook() {
        search(AuthorTitle("J.K Rowling", "Harry Potter and the Philosopher's Stone")) { r ->
            assertEquals(5, r.items.size)
        }
    }

    @Test
    fun imageLinksPrefersLargeImageWhenAllPresent() {
        val imageLinks = ImageLinks("small", "medium", "large", "extraLarge")
        assertEquals("large", imageLinks.bestAvailable())
    }

    @Test
    fun imageLinksFallsBackToMediumImageWhenLargeIsNotPresent() {
        val imageLinks = ImageLinks("small", "medium", null, "extraLarge")
        assertEquals("medium", imageLinks.bestAvailable())
    }

    @Test
    fun downloadsCover() {
        val bookDirectory = createTempDir()
        var called = false
        val volume = Volume(VolumeInfo("Animal Farm"), "https://www.googleapis.com/books/v1/volumes/AQLJ2IxOvOAC")
        val book = Book(AuthorTitle("1", "2"), listOf(Chapter(File(bookDirectory, "1.mp3"))))
        cover(volume, book) { called = true }
        assertTrue(book.cover.exists())
        assertFalse(book.cover.readBytes().isEmpty())
        assertTrue("callback should have been invoked", called)
    }

    @Test
    fun normalisesStringsForMatching() {
        assertEquals("jkrowling", normalise("J . k RoWling"))
        assertEquals("philosophersstone", normalise("Philosopher's Stone"))
    }

}
