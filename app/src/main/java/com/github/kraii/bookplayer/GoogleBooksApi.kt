package com.github.kraii.bookplayer

import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.File
import java.nio.charset.Charset

private val tag = "booksApi"

data class SearchResult(val items: List<Volume>)

data class Volume(val volumeInfo: VolumeInfo, val selfLink: String) {
    fun matches(authorTitle: AuthorTitle): Boolean = volumeInfo.matches(authorTitle)
}

data class VolumeInfo(
        val title: String,
        val authors: List<String>? = null,
        val imageLinks: ImageLinks? = null
) {
    fun matches(authorTitle: AuthorTitle): Boolean {
        return normalise(authorTitle.title) == normalise(title)
                && authors?.map(::normalise)?.contains(normalise(authorTitle.author)) ?: false
    }
}

private val normaliseRegex =  "[\\s.,']".toRegex()
internal fun normalise(s: String) : String = s.replace(normaliseRegex, "").toLowerCase()

data class ImageLinks(
        val small: String?,
        val medium: String?,
        val large: String?,
        val extraLarge: String?
) {
    fun bestAvailable(): String? {
        return large ?: medium ?: small ?: extraLarge
    }
}

private fun readSearchResult(data: String): SearchResult? = searchResultAdapter.fromJson(data)

private fun readVolume(data: String): Volume? = volumeAdapter.fromJson(data)

fun downloadCovers(books: List<Book>, callback: (Book) -> Unit) {
    books.filter { it.needsCover() }
            .forEach { book ->
                search(book.authorTitle) { (items) ->
                    val chosenVolume = chooseVolume(book.authorTitle, items)
                    if (chosenVolume != null) {
                        cover(chosenVolume, book, { callback(book) })
                    } else {
                        Log.w(tag, "Failed to find matching volume for ${book.authorTitle} in $items")
                    }
                }
            }
}

private fun chooseVolume(authorTitle: AuthorTitle, volumes: List<Volume>): Volume? {
    return volumes.find { volume -> volume.matches(authorTitle) }
}

internal fun search(book: AuthorTitle, consumer: (SearchResult) -> Unit) {
    Log.i(tag, "Searching for $book")
    val params = listOf("q" to "${book.author} ${book.title}", "maxResults" to 5, "projection" to "lite")
    "https://www.googleapis.com/books/v1/volumes".httpGet(params).responseString { request, _, result ->
        result.fold({ data ->
            Log.i(tag, "Found $book")
            val searchResult = readSearchResult(data)
            if (searchResult == null) {
                Log.w("books-api", "No data found for $book")
            } else {
                consumer(searchResult)
            }
        }, errorHandler(request))
    }
}

internal fun cover(searchResultVolume: Volume, book: Book, callback: (File) -> Unit) {
    Log.i(tag, "Downloading cover for ${searchResultVolume.volumeInfo.title} to $book")
    searchResultVolume.selfLink.httpGet().responseString { request, _, result ->
        result.fold({ data ->
            val volume = readVolume(data)
            if (volume == null) {
                Log.w("books-api", "No volume found for ${searchResultVolume.selfLink}")
            } else {
                val imageUrl = volume.volumeInfo.imageLinks?.bestAvailable()
                Log.i(tag, "downloading $imageUrl")
                imageUrl?.httpDownload()?.destination { _, _ ->
                    book.coverFile()
                }?.response { _, _, downloadResult ->
                    when (downloadResult) {
                        is Result.Success -> {
                            book.cover = book.coverFile()
                            callback(book.coverFile())
                        }
                    }
                }
            }
        }, errorHandler(request))
    }
}

private fun errorHandler(request: Request): (FuelError) -> Unit {
    return { error ->
        Log.e(
                "books-api",
                "Failed api call for ${request.url} - ${request.parameters}," +
                        " Response: ${error.response.httpStatusCode} " +
                        "- ${error.errorData.toString(Charset.defaultCharset())}" +
                        "- $error"

        )
    }
}

private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


private val searchResultAdapter: JsonAdapter<SearchResult> = moshi.adapter(SearchResult::class.java)
private val volumeAdapter: JsonAdapter<Volume> = moshi.adapter(Volume::class.java)
