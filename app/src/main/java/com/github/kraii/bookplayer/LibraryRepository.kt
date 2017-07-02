package com.github.kraii.bookplayer

import android.content.Context
import com.squareup.moshi.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import java.io.File

data class LibraryJson(
        val selectedTitle: AuthorTitle?,
        val books: List<Book>
) {
    fun toLibrary(): Library = Library(books, selectedTitle)
}

internal class FileJsonAdapter : JsonAdapter<File>() {
    override fun toJson(writer: JsonWriter, value: File?) {
        writer.value(value?.path)
    }

    override fun fromJson(reader: JsonReader): File? {
        val path = reader.nextString()
        return path?.let { File(it) }
    }
}

class LibraryRepository : AnkoLogger {
    val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(File::class.java, FileJsonAdapter())
            .build()
    val libraryAdapter: JsonAdapter<LibraryJson> = moshi.adapter(LibraryJson::class.java)

    internal fun toJson(library: Library): String {
        val libraryCopy = LibraryJson(
                library.selectedTitle()?.authorTitle,
                library.books
        )
        return libraryAdapter.toJson(libraryCopy)
    }

    internal fun fromJson(json: String): Library {
        val libraryJson = libraryAdapter.fromJson(json)
        return libraryJson?.toLibrary() ?: emptyLibrary()
    }

    fun save(context: Context, library: Library) {
        libraryFile(context).writeText(toJson(library))
    }

    private fun libraryFile(context: Context) = File(context.filesDir, "library.json")

    fun load(context: Context): Library {
        val libraryFile = libraryFile(context)
        if (libraryFile.exists()) {
            try {
                val json = libraryFile.readText()
                return fromJson(json)
            } catch (e: JsonDataException) {
                warn("Library corrupted, did not load")
                return emptyLibrary()
            }
        } else {
            return emptyLibrary()
        }
    }
}