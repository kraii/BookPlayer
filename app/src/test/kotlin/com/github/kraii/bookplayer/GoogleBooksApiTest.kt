package com.github.kraii.bookplayer

import com.github.kittinunf.fuel.Fuel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class GoogleBooksApiTest {

    @Before
    fun setUpFuel() {
        Fuel.testMode {
            timeout = 5000
        }
    }

    @Test
    fun searchForBook() {
        search(AuthorTitle("George Orwell", "Animal Farm")) { r ->
            print(r)
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
        val file = createTempFile()
        var called = false
        val volume = Volume(VolumeInfo("Animal Farm"), "https://www.googleapis.com/books/v1/volumes/AQLJ2IxOvOAC")
        cover(volume, file) { called = true }
        println(file.path)
        assertTrue(file.exists())
        assertFalse(file.readBytes().isEmpty())
        assertTrue("callback should have been invoked", called)
    }

}
