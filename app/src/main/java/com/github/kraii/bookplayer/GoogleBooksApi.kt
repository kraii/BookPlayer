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

data class SearchResult(val items: List<Volume>)

data class Volume(val volumeInfo: VolumeInfo, val selfLink: String)

data class VolumeInfo(
        val title: String,
        val authors: List<String>? = null,
        val imageLinks: ImageLinks? = null
)

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

private fun readSearchResult(data: String): SearchResult? {
    return searchResultAdapter.fromJson(data)
}

private fun readVolume(data: String): Volume? {
    return volumeAdapter.fromJson(data)
}

fun search(book: AuthorTitle, consumer: (SearchResult) -> Unit) {
    val params = listOf("q" to "${book.author} ${book.title}", "maxResults" to 5, "projection" to "lite")
    "https://www.googleapis.com/books/v1/volumes".httpGet(params).responseString { request, _, result ->
        result.fold({ data ->
            val searchResult = readSearchResult(data)
            if (searchResult == null) {
                Log.w("books-api", "No data found for $book")
            } else {
                consumer(searchResult)
            }
        }, errorHandler(request))
    }
}

fun cover(searchResultVolume: Volume, destination: File, callback: (File) -> Unit) {
    searchResultVolume.selfLink.httpGet().responseString { request, _, result ->
        result.fold({ data ->
            val volume = readVolume(data)
            if (volume == null) {
                Log.w("books-api", "No volume found for ${searchResultVolume.selfLink}")
            } else {
                val imageUrl = volume.volumeInfo.imageLinks?.bestAvailable()
                imageUrl?.httpDownload()?.destination { _, _ ->
                    destination
                }?.response { _, _, downloadResult ->
                    when(downloadResult) {
                        is Result.Success -> {
                            callback(destination)
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
                        "- ${error.errorData.toString(Charset.defaultCharset())}"
        )
    }
}

private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


private val searchResultAdapter: JsonAdapter<SearchResult> = moshi.adapter(SearchResult::class.java)
private val volumeAdapter: JsonAdapter<Volume> = moshi.adapter(Volume::class.java)
